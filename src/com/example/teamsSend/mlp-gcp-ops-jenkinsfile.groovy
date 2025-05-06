def Prepare_and_Build(slackResponse ,BUILD_PATH, IMAGE) {
    try {
        echo "Build ${IMAGE}"
        pbslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Build ${BUILD_PATH}.")
        sh 'pushd ${BUILD_PATH} && docker build --no-cache --network host --tag ${IMAGE}:$GIT_COMMIT .'
        slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Build ${BUILD_PATH}.", timestamp: pbslackResponse.ts)
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Build ${BUILD_PATH}.", timestamp: pbslackResponse.ts)
        throw exc
    }
}
def Tag_and_Push(slackResponse, BUILD_PATH, IMAGE) {
    try {
        echo 'Tag DEV'
        tpslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Tag & Push ${BUILD_PATH}.")
        sh 'docker tag ${IMAGE}:$GIT_COMMIT ${DEV_REPO}/${IMAGE}:$GIT_COMMIT'
        echo 'Tag PRODUCTION'
        sh 'docker tag ${DEV_REPO}/${IMAGE}:$GIT_COMMIT ${PROD_REPO}/${IMAGE}:$GIT_COMMIT'
        echo 'Push registry'
        sh 'docker push ${DEV_REPO}/${IMAGE}:$GIT_COMMIT'
        slackSend(channel: slackResponse.threadId, color: '#FFB500', message: "IMAGE TO DEPLOY IN DEV: `${DEV_REPO}/${IMAGE}:${env.GIT_COMMIT}`")
        sh 'docker push ${PROD_REPO}/${IMAGE}:$GIT_COMMIT'
        slackSend(channel: slackResponse.threadId, color: '#FFB500', message: "IMAGE TO DEPLOY IN PRODUCTION: `${PROD_REPO}/${IMAGE}:${env.GIT_COMMIT}`")
        slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Tag & Push ${BUILD_PATH}.", timestamp: tpslackResponse.ts)
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Tag & Push ${BUILD_PATH}.", timestamp: tpslackResponse.ts)
        throw exc
    }
}
def Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE) {
    try {
        retry(3) {
            echo "Scan ${IMAGE}"
            scanslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Scan Vulnerability ${BUILD_PATH}.")
            sh 'trivy image --cache-dir /tmp/trivy/ --exit-code 1 --ignore-unfixed --severity HIGH,CRITICAL ${IMAGE}:$GIT_COMMIT'
            slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Scan Vulnerability ${BUILD_PATH}.", timestamp: scanslackResponse.ts)
        }
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Scan Vulnerability ${BUILD_PATH}.", timestamp: scanslackResponse.ts)
        throw exc
    }
}
def Clean_image(slackResponse, BUILD_PATH, IMAGE) {
    try {
        echo "Delete image ${IMAGE}"
        cleanslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Clean image ${BUILD_PATH}.")
        sh 'docker image rm ${IMAGE}:$GIT_COMMIT'
        slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Clean image ${BUILD_PATH}.", timestamp: cleanslackResponse.ts)
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Clean image ${BUILD_PATH}.", timestamp: cleanslackResponse.ts)
        throw exc
    }
}
def Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE) {
    try {
        echo "Delete tagged image ${IMAGE}"
        cleantaggedslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Clean tagged images ${BUILD_PATH}.")
        sh 'docker image rm ${DEV_REPO}/${IMAGE}:$GIT_COMMIT'
        slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Clean DEV image ${BUILD_PATH}.", timestamp: cleantaggedslackResponse.ts)
        sh 'docker image rm ${PROD_REPO}/${IMAGE}:$GIT_COMMIT'
        slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Clean tagged images ${BUILD_PATH}.", timestamp: cleantaggedslackResponse.ts)
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Clean tagged images ${BUILD_PATH}.", timestamp: cleantaggedslackResponse.ts)
        throw exc
    }
}

pipeline {
    agent any
    environment {
        PROD_REPO = 'us.gcr.io/mlp-infra-services-0286b146845'
        DEV_REPO = 'us.gcr.io/big-data-poc-216405'
    }
    stages {
        stage('Init pipeline') {
            steps {
                script {
                    slackResponse = slackSend(color: '#2986cc', message: ":loading2: Started mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)")
                }
            }
        }
        stage('Docker Pipelines') {
            failFast false
            parallel {
                stage('clamav') {
                    environment {
                        BUILD_PATH = 'gcp-cos-clamav'
                        IMAGE = 'mlp-clamav'
                    }
                    when {
                        changeset 'gcp-cos-clamav/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('basic-fim') {
                    environment {
                        BUILD_PATH = 'gcp-cos-basic-fim'
                        IMAGE = 'mlp-basic-fim'
                    }
                    when {
                        changeset 'gcp-cos-basic-fim/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)            }
                    }
                }
                stage('mlp-tomcat9-jre8') {
                    environment {
                        BUILD_PATH = 'mlp-tomcat9-jre8'
                        IMAGE = 'mlp-tomcat9-jre8'
                    }
                    when {
                        changeset 'mlp-tomcat9-jre8/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-tomcat9-jre11') {
                    environment {
                        BUILD_PATH = 'mlp-tomcat9-jre11'
                        IMAGE = 'mlp-tomcat9-jre11'
                    }
                    when {
                        changeset 'mlp-tomcat9-jre11/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-tomcat9-jre17') {
                    environment {
                        BUILD_PATH = 'mlp-tomcat9-jre17'
                        IMAGE = 'mlp-tomcat9-jre17'
                    }
                    when {
                        changeset 'mlp-tomcat9-jre17/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-tomcat10-jre17') {
                    environment {
                        BUILD_PATH = 'mlp-tomcat10-jre17'
                        IMAGE = 'mlp-tomcat10-jre17'
                    }
                    when {
                        changeset 'mlp-tomcat10-jre17/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-ubuntu-jre17-headless') {
                    environment {
                        BUILD_PATH = 'mlp-ubuntu-jre17-headless'
                        IMAGE = 'mlp-ubuntu-jre17-headless'
                    }
                    when {
                        changeset 'mlp-ubuntu-jre17-headless/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-stunnel') {
                    environment {
                        BUILD_PATH = 'mlp-stunnel'
                        IMAGE = 'mlp-stunnel'
                    }
                    when {
                        changeset 'mlp-stunnel/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-pre-commit') {
                    environment {
                        BUILD_PATH = 'mlp-pre-commit'
                        IMAGE = 'mlp-pre-commit'
                    }
                    when {
                        changeset 'mlp-pre-commit/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-jmx-exporter') {
                    environment {
                        BUILD_PATH = 'mlp-jmx-exporter'
                        IMAGE = 'mlp-jmx-exporter'
                    }
                    when {
                        changeset 'mlp-jmx-exporter/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
                stage('mlp-maven-dind') {
                    environment {
                        BUILD_PATH = 'mlp-maven-dind'
                        IMAGE = 'mlp-maven-dind'
                    }
                    when {
                        changeset 'mlp-maven-dind/**'
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            if ( env.BRANCH_NAME == 'master' ) {
                                Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                                Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            }
                            Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
                    }
                }
            }
        }
        stage('Groovy Pipelines') {
            agent {
                docker {
                    image 'nvuillam/npm-groovy-lint:v15.1.0'
                    args '--entrypoint='
                    reuseNode true
                }
            }
            when {
                anyOf { changeset 'mlp-pipelines/**'; changeset 'Jenkinsfile' }
            }
            options {
                timeout(time: 5, unit: 'MINUTES')
            }
            steps {
                script {
                    precoslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ':loading2: Groovy Lint Stages')
                    try {
                        // sh 'npm-groovy-lint --version'
                        sh 'npm-groovy-lint --failon=error **/*.groovy **/Jenkinsfile'
                        slackSend(channel: slackResponse.threadId, color: 'good', message: ':large_green_circle: Groovy Lint Stages', timestamp: precoslackResponse.ts)
                    }
                    catch (exc) {
                        slackSend(channel: slackResponse.threadId, color: 'danger', message: ':red_circle: Groovy Lint Stages', timestamp: precoslackResponse.ts)
                        currentBuild.result = 'FAILURE'
                        throw exc
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'This will always run'
        }
        success {
            slackSend( channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)", timestamp: slackResponse.ts)
        }
        failure {
            slackSend( channel: slackResponse.threadId, color: 'danger', message: ":red_circle: mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)", timestamp: slackResponse.ts)
        }
        unstable {
            slackSend( channel: slackResponse.threadId, color: 'danger', message: ":large_orange_circle: mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)", timestamp: slackResponse.ts)
        }
        changed {
            slackSend( channel: slackResponse.threadId, color: 'warning', message: ":large_yellow_circle: mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)", timestamp: slackResponse.ts)
        }
    }
}
