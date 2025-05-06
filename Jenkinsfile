import com.example.teamsSend.MsTeamsHelper


def Prepare_and_Build(slackResponse ,BUILD_PATH, IMAGE) {

    def teamsHelper = new MsTeamsHelper()
    try {
        echo "Build ${IMAGE}"
        // pbslackResponse = slackSend(channel: slackResponse.threadId, color: '#2986cc', message: ":loading2: Build ${BUILD_PATH}.")
        pbslackResponse = teamsHelper.teamsSend(
            "${env.TEAMS_WEBHOOK_URL}",
            'reply',
            "${env.TEAMS_TEAM_NAME}",
            "${env.TEAMS_CHANNEL_NAME}",
            slackResponse.threadId,
            '',
            'loading',
            "Build ${BUILD_PATH} initial reply.",
            "Build ${BUILD_PATH} initial reply.",
            { args -> httpRequest(args) } // Pass httpRequest as a closure
        )

        // sh 'pushd ${BUILD_PATH} && docker build --no-cache --network host --tag ${IMAGE}:$GIT_COMMIT .'
        // slackSend(channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: Build ${BUILD_PATH}.", timestamp: pbslackResponse.ts)
        teamsHelper.teamsSend(
            "${env.TEAMS_WEBHOOK_URL}",
            'reply',
            "${env.TEAMS_TEAM_NAME}",
            "${env.TEAMS_CHANNEL_NAME}",
            slackResponse.threadId,
            pbslackResponse.replyId,
            'success',
            "Build ${BUILD_PATH} completed successfully.",
            "Build ${BUILD_PATH} completed successfully.",
            { args -> httpRequest(args) } // Pass httpRequest as a closure
        )
    }
    catch (exc) {
        slackSend(channel: slackResponse.threadId, color: 'danger', message: ":red_circle: Build ${BUILD_PATH}.", timestamp: pbslackResponse.ts)
        teamsHelper.teamsSend(
            "${env.TEAMS_WEBHOOK_URL}",
            'reply',
            "${env.TEAMS_TEAM_NAME}",
            "${env.TEAMS_CHANNEL_NAME}",
            slackResponse.threadId,
            pbslackResponse.replyId,
            'failure',
            "Build ${BUILD_PATH} failed.",
            "Build ${BUILD_PATH} failed.",
            { args -> httpRequest(args) } // Pass httpRequest as a closure
        )
        currentBuild.result = 'FAILURE'
        throw exc
    }
}

/*
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
*/

pipeline {
    agent any
    environment {
        PROD_REPO = 'us.gcr.io/mlp-infra-services-0286b146845'
        DEV_REPO = 'us.gcr.io/big-data-poc-216405'
        TEAMS_WEBHOOK_URL = "https://prod-75.westus.logic.azure.com:443/workflows/3e0509a69771439d88e32ace24787150/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=OnsFqs9e0Pwp-u4-r30WECTnmYRKhHg2PwOYwNK7vmY"
        TEAMS_TEAM_NAME = 'Slack Transition Test Team'
        TEAMS_CHANNEL_NAME = 'TestReplyPostsUpdate'
        GIT_BRANCH = "main"
        RUN_DISPLAY_URL = "http://example.com/run"
        BUILD_NUMBER = currentBuild.number
    }
    stages {
        stage('Init pipeline') {
            steps {
                script {
                    //def BUILD_NUMBER = currentBuild.number

                    def teamsHelper = new MsTeamsHelper()
                    slackResponse = teamsHelper.teamsSend(
                        "${env.TEAMS_WEBHOOK_URL}",
                        'post',
                        "${env.TEAMS_TEAM_NAME}",
                        "${env.TEAMS_CHANNEL_NAME}",
                        '',
                        '',
                        'loading',
                        'Build Notification from Jenkins',
                        "Started mlp-gcp-ops » ${env.GIT_BRANCH} #${env.BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)",
                        { args -> httpRequest(args) } // Pass httpRequest as a closure
                    )
                    echo "Root Post Thread ID: ${slackResponse.threadId}"

                    // slackResponse = slackSend(color: '#2986cc', message: ":loading2: Started mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)")
                }
            }
        }
        stage('Docker Pipelines') {
            failFast false
            parallel {
                stage('clamav') {
                    environment {
                        BUILD_PATH = 'gcp-cos-clamav-3'
                        IMAGE = 'mlp-clamav'
                    }
                    when {
                        //changeset 'gcp-cos-clamav/**'
                         expression { true }
                    }
                    steps {
                        script {
                            Prepare_and_Build(slackResponse, BUILD_PATH, IMAGE)
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)            }
                    }
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // Scan_Vulnerability(slackResponse, BUILD_PATH, IMAGE)
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
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
                            // if ( env.BRANCH_NAME == 'master' ) {
                            //     Tag_and_Push(slackResponse, BUILD_PATH, IMAGE)
                            //     Clean_tagged_image(slackResponse, BUILD_PATH, IMAGE)
                            // }
                            // Clean_image(slackResponse, BUILD_PATH, IMAGE)
                        }
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
            //slackSend( channel: slackResponse.threadId, color: 'good', message: ":large_green_circle: mlp-gcp-ops » ${env.GIT_BRANCH} #${BUILD_NUMBER} (<${RUN_DISPLAY_URL}|Open>)", timestamp: slackResponse.ts)
            teamsHelper.teamsSend(
                        "${env.TEAMS_WEBHOOK_URL}",
                        'post',
                        "${env.TEAMS_TEAM_NAME}",
                        "${env.TEAMS_CHANNEL_NAME}",
                        slackResponse.threadId,
                        '',
                        'success',
                        'Build Notification from Jenkins',
                        "mlp-gcp-ops » ${env.GIT_BRANCH} #${env.BUILD_NUMBER} (<${env.RUN_DISPLAY_URL}|Open>)",
                        { args -> httpRequest(args) } // Pass httpRequest as a closure
                    )
        }
       
    }
        
    }

