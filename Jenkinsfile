pipeline {
  agent any
  
  environment {
    VERSION=""
    JAR_NAME=""
    S3BUCKET="rba-application-code"
    S3PREFIX="Canopy/FNOL/corda"
    DATAMIGRATIONTOOL="corda-tools-database-manager-3.1.jar"
    DATAMIGRATIONTOOLBUCKET="rba-adop/jar"
  }
  
  stages {
    stage('Workspace Preparation') {
      steps {
        echo "Cleaning up workspace..."
        cleanWs()
      }
    }
    
    stage('Clone Repository') {
      steps {
        echo "Cloning Git repository from CodeCommit..."
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'svc_rb_devops_codecommit', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
          git branch: 'development', url: 'https://git-codecommit.us-east-1.amazonaws.com/v1/repos/fnolCorda', credentialsId: 'svc_rb_devops_codecommit'
        }
      }
    }
    
    stage('Unit Testing') {
      steps{
        sh 'chmod +x gradlew'
        echo "Beginning unit tests..."
        sh './gradlew clean test jacocoTestReport'
      }
    }
    
    stage('SonarQube Analysis') {
      steps {
        echo "Beginning code analysis with SonarQube..."
        withSonarQubeEnv('ADOP SonarQube') {
          sh './gradlew --info sonarqube --stacktrace -D sonar-project.properties' 
        }
      }
    }

    stage('Build Application') {
      steps {
        echo "Building application code..."
        sh './gradlew clean build'
      }
    }

    stage('Create Data Schema') {
      environment {
        VERSION=sh(
                  returnStdout: true,
                  script: 'cat gradle.properties | grep version | cut -d \"=\" -f 2'
                ).trim()
        JAR_NAME=sh(
                  returnStdout: true,
                  script: 'cat gradle.properties | grep name | cut -d \"=\" -f 2'
                ).trim()
      }
      steps {
        echo "Preparing data schema migration..."
        
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'DevPostgres', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
          sh '''
            echo "Creating cordapp/node.conf..."
            echo \"myLegalName=\\"O=Migration, L=WOH, C=US\\"\" &>> ./node.conf
            echo \"\"  &>> ./node.conf
            
            echo \"p2pAddress:\\"localhost:1003\\"\" &>> ./node.conf
            echo \"\" &>> ./node.conf
            
            echo \"rpcSettings = {\" &>> ./node.conf
            echo \"     standAloneBroker = false\" &>> ./node.conf
            echo \"     address : \\"localhost:10033\\"\" &>> ./node.conf
            echo \"     adminAddress : \\"localhost:10034\\"\" &>> ./node.conf
            echo \"}\" &>> ./node.conf
            echo \"\" &>> ./node.conf
            
            echo \"dataSourceProperties = {\" &>> ./node.conf
            echo \"   dataSourceClassName = \\"org.postgresql.ds.PGSimpleDataSource\\"\" &>> ./node.conf
            echo \"   dataSource.url = \\"jdbc:postgresql://acnriskblockdb.ck3rxt6utknv.us-east-1.rds.amazonaws.com:5432/data_migration_db\\"\" &>> ./node.conf
            echo \"   dataSource.user = \\"$USERNAME\\"\" &>> ./node.conf
            echo \"   dataSource.password = \\"$PASSWORD\\"\" &>> ./node.conf
            echo \"}\" &>> ./node.conf
            echo \"\" &>> ./node.conf
            
            echo \"rpcUsers=[\" &>> ./node.conf
            echo \"    {\" &>> ./node.conf
            echo \"        password=test\" &>> ./node.conf
            echo \"        permissions=[\" &>> ./node.conf
            echo \"             ALL\" &>> ./node.conf
            echo \"        ]\" &>> ./node.conf
            echo \"        user=user1\" &>> ./node.conf
            echo \"    }\" &>> ./node.conf
            echo \"]\" &>> ./node.conf
          '''
        }
        
        echo "Pulling down JDBC driver(s)..." 
        sh '''
          mkdir ./drivers
          aws s3 cp s3://rba-adop/drivers/postgresql-42.1.4.jre7.jar ./drivers/.
        '''
          
        echo "Pulling down Corda Tools Database Manager..."
        sh 'aws s3 cp s3://$DATAMIGRATIONTOOLBUCKET/$DATAMIGRATIONTOOL .'
    
        echo "Creating data schema..." 

        sh '''
          FULLPATH=\$(find . -name $JAR_NAME-$VERSION*.jar)
          BASENAME=\$(basename \$(find . -name $JAR_NAME-$VERSION*.jar))
              
          mkdir ./cordapps
          cp $FULLPATH ./cordapps
              
          java -jar \$DATAMIGRATIONTOOL --base-directory . --create-migration-sql-for-cordapp
              
          rm -rf ./cordapps
              
          zip -r data_schema_$BASENAME.zip migration
          mv data_schema_$BASENAME.zip data_schema_$BASENAME
        '''
      }
    }
    
    stage('Uploading Artifact(s)') {
      environment {
        VERSION=sh(
                  returnStdout: true,
                  script: 'cat gradle.properties | grep version | cut -d \"=\" -f 2'
                ).trim()
        JAR_NAME=sh(
                  returnStdout: true,
                  script: 'cat gradle.properties | grep name | cut -d \"=\" -f 2'
                ).trim()
      }
      steps {
        sh '''
          echo \"Finding the location of the application jar...\"

          APPFILEPATH=\$(dirname \$(find . -name $JAR_NAME-$VERSION*.jar))
          APPBASENAME=\$(basename \$(find . -name $JAR_NAME-$VERSION*.jar))
          echo \"Application JAR Name: \$APPBASENAME\"
          echo \"Application JAR Path: \$APPFILEPATH\"

          echo \"Uploading to S3...\"
          cd \$APPFILEPATH
          DATE=\$(date --utc +%FT%TZ)
          aws s3api put-object --body \$APPBASENAME --key \$S3PREFIX/\$VERSION/\$APPBASENAME --bucket \$S3BUCKET --tagging \"revisionID=\$GIT_COMMIT&releaseVersion=\$VERSION&repository=$JAR_NAME&artifactName=\$APPBASENAME&date=\$DATE\" |& tee upload_log.txt
          echo \"Application jar was uploaded successfully at $DATE\"
         
          S3APPVERSION=\$(cat upload_log.txt | grep Version | cut -d \":\" -f 2 | cut -d \"\\"\" -f 2)
          echo \"Application S3 Version: \$s3Version\"
  
          GIT_COMMIT=\$(git log -n 1 --pretty=format:'%H')
          GIT_AUTHOR=\$(git log -1 --pretty=format:'%an')
          GIT_MESSAGE=\$(git log --format=%B -n 1)
          
          cd $WORKSPACE
          
          echo \"Finding the location of the data schema jar...\"
          DATAFILEPATH=\$(dirname \$(find . -name data_schema*.jar))
          DATABASENAME=\$(basename \$(find . -name data_schema*.jar))
          echo \"Data Schema JAR Name: \$DATABASENAME\"
          echo \"Data Schema JAR Path: \$DATAFILEPATH\"
        
          cd $DATAFILEPATH

          echo \"Uploading to S3...\"
          aws s3api put-object --body \$DATABASENAME --key \$S3PREFIX/\$VERSION/\$DATABASENAME --bucket \$S3BUCKET --tagging \"revisionID=\$GIT_COMMIT&releaseVersion=\$VERSION&repository=$JAR_NAME&artifactName=\$DATABASENAME&date=\$DATE\" |& tee upload_log.txt
          S3DATAVERSION=\$(cat upload_log.txt | grep Version | cut -d \":\" -f 2 | cut -d \"\\"\" -f 2)
          echo \"Data Schema S3 Version: \$S3DATAVERSION\"

          cd $WORKSPACE

          echo \"Writing application artifact metadata...\"
          echo \"{ \\"Environment\\": {\\"S\\": \\"dev\\"},\" &>> appMetadata.json
          echo \"\\"UseCase\\": {\\"S\\": \\"$JAR_NAME\\"},\" &>> appMetadata.json
          echo \"\\"CanopyVersion\\": {\\"S\\": \\"\$VERSION\\"},\" &>> appMetadata.json
          echo \"\\"s3Version\\": {\\"S\\": \\"\$S3APPVERSION\\"}, \" &>> appMetadata.json
          echo \"\\"CodeCommitID\\": {\\"S\\": \\"\$GIT_COMMIT\\"},\" &>> appMetadata.json
          echo \"\\"Date\\": {\\"S\\": \\"\$DATE\\"} }\" &>> appMetadata.json
          cat appMetadata.json
          
          echo \"Writing data schema artifact metadata...\"
          echo \"{ \\"Environment\\": {\\"S\\": \\"dev\\"},\" &>> dataSchemaMetadata.json
          echo \"\\"UseCase\\": {\\"S\\": \\"data_schema_$JAR_NAME\\"},\" &>> dataSchemaMetadata.json
          echo \"\\"CanopyVersion\\": {\\"S\\": \\"\$VERSION\\"},\" &>> dataSchemaMetadata.json
          echo \"\\"s3Version\\": {\\"S\\": \\"\$S3DATAVERSION\\"}, \" &>> dataSchemaMetadata.json
          echo \"\\"CodeCommitID\\": {\\"S\\": \\"\$GIT_COMMIT\\"},\" &>> dataSchemaMetadata.json
          echo \"\\"Date\\": {\\"S\\": \\"\$DATE\\"} }\" &>> dataSchemaMetadata.json
          cat dataSchemaMetadata.json

          echo \"Uploading metadata to DynamoDB...\"
          aws dynamodb put-item --table-name rba-environment-status --item file://appMetadata.json
          
          echo \"Uploading metadata to DynamoDB...\"
          aws dynamodb put-item --table-name rba-environment-status --item file://dataSchemaMetadata.json
          
          touch artifacts_uploaded.txt
          
          echo \"Creating flat file for promotion process...\" >> artifacts_uploaded.txt
          echo \"dev_commit_id=$GIT_COMMIT\"
          echo \"dev_application_jar=\$APPBASENAME\" >> artifacts_uploaded.txt
          echo \"dev_application_version=\$S3APPVERSION\" >> artifacts_uploaded.txt
          echo \"dev_data_schema_jar=\$DATABASENAME\" >> artifacts_uploaded.txt
          echo \"dev_data_schema_version=\$S3DATAVERSION\" >> artifacts_uploaded.txt
          
          echo \"Uploading to S3..."
          aws s3api put-object --body artifacts_uploaded.txt  --key \$S3PREFIX/\$VERSION/artifacts_uploaded.txt --bucket \$S3BUCKET --tagging \"revisionID=\$GIT_COMMIT&releaseVersion=\$VERSION&repository=$JAR_NAME&date=\$DATE\"
        '''
      }
    }
  }
}





