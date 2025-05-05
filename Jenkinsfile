@Library('pk-shared-lib')
import groovy.json.JsonSlurper

pipeline {
    agent any
    
    environment {
        // Teams configuration
        TEAMS_WEBHOOK_URL = "https://prod-75.westus.logic.azure.com:443/workflows/3e0509a69771439d88e32ace24787150/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=OnsFqs9e0Pwp-u4-r30WECTnmYRKhHg2PwOYwNK7vmY"
        TEAMS_TEAM_NAME = 'Slack Transition Test Team'
        TEAMS_CHANNEL_NAME = 'TestReplyPostsUpdate'
        BUILD_PATH = 'gcp-cos-clamav-05May 22:58pm ist'
        IMAGE = 'mlp-clamav'
    }

    stages {
        stage('Send Teams Message') {
            steps {
                script {
                    try {

                        // teams send for root post
                        def response = teamsSend(
                        "${env.TEAMS_WEBHOOK_URL}",
                            'post',
                        "${env.TEAMS_TEAM_NAME}",
                            "${env.TEAMS_CHANNEL_NAME}",
                            '',
                            '',
                            'loading',
                            'Build Notification from Jenkins 05May2025',
                            'Build completed successfully from Jenkins!'
                        )

                        if (!response?.content) {
                            error("Response content is null or empty!")
                        }

                        echo "Root Response: ${response.content}"
                        
                        // Parse the response content
                        def parsedTeamsResponse = new JsonSlurper().parseText(response.content) as HashMap
                        echo "Thread ID: ${parsedTeamsResponse.threadId}"
                        

                        def replyteamsResponse = teamsSend(
                            "${env.TEAMS_WEBHOOK_URL}",
                            'reply',
                            "${env.TEAMS_TEAM_NAME}",
                            "${env.TEAMS_CHANNEL_NAME}",
                            parsedTeamsResponse.threadId,
                            '',
                            'loading',
                            "Build ${BUILD_PATH} initial reply.",
                            "Build ${BUILD_PATH} initial reply."
                        )

                        echo "First reply posted"
                        echo "${replyteamsResponse}"

                        // Convert LazyMap to HashMap
                        if (replyteamsResponse?.content) {
                            def parsedResponse = new JsonSlurper().parseText(replyteamsResponse.content) as HashMap
                            echo "Parsed threadId: ${parsedResponse.threadId}"
                        } else {
                            error("replyteamsResponse.content is null or empty!")
                        }
                    } catch (exc) {
                        throw exc
                    }
                    
                }
            }
        }
    }
}
