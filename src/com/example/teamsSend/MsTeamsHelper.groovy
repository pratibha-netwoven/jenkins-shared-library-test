import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class MsTeamsHelper {    
     /**
    * Sends a formatted Adaptive Card message to a Microsoft Teams channel via webhook anonymous.
    *
    * Usage:
    * teamsSend(
    *     teamsWebhookUrl,     // (Required) Teams webhook URL.
    *     type,                // (Required) 'post' or 'reply'.
    *     teamsTeamName,       // (Required) Title of the Teams team.
    *     teamsChannelName,    // (Required) Title of the Teams channel.
    *     threadId,            // (Optional) Leave blank for new root post.
    *                          //           Provide threadId to update a root post or reply to an existing post.
    *     replyId,             // (Optional) Leave blank for new reply.
    *                          //           Provide replyId to update an existing reply.
    *     status,              // (Required) Message status: 'loading', 'success', or 'failure'.
    *     msgTitle,            // (Required) Title of the post or reply.
    *     msgBody              // (Required) Body content of the post or reply.
    * )
    *
    * Workflow:
    * 1. Builds the message payload with status/type-specific formatting.
    * 2. Sends the payload to the specified Teams webhook URL.
    * 3. Returns the parsed response object.
    *
    * Response Structure:
    * {
    *   "replyId"  : "<<replyId>>",  // Message ID of the reply posted
                                        (the property will be absent in case of the flow of root post creation).
    *   "threadId" : "<<threadId>>"  // Message ID of the root post (thread ID).
    * }
    */
    static def teamsSend(String teamsWebhookUrl, String type, String teamsTeamName, String teamsChannelName, String threadId, String replyId, String status, String msgTitle, String msgBody) 
    {
        //  Step 1: Get the URL
        String url = teamsWebhookUrl

        //  Step 2: Build the Payload for creating the msteams post with Adaptive Card based on the type of message
        def payload = buildTeamsMessagePayloadWithAdaptiveCard(type,
                                                        teamsTeamName,
                                                        teamsChannelName,
                                                        threadId,
                                                        replyId,
                                                        status,
                                                        msgTitle,
                                                        msgBody)

        // //  Step 3: Call API and return parsed response
        def response = sendMessageToTeamsUsingWebhook(url, payload)        
        return response        
    }

    @NonCPS
    static Map sendMessageToTeamsUsingWebhook(String url, Map payload) {       

        def jsonPayload = new groovy.json.JsonBuilder(payload).toPrettyString()

        def response = httpRequest(
            httpMode: 'POST',
            url: url,
            contentType: 'APPLICATION_JSON',
            requestBody: jsonPayload,
            validResponseCodes: '200:299',
            consoleLogResponseBody: true
        )

        echo "Response Code: ${response.status}"
        echo "Response Body from code: ${response}"

        // Optionally parse JSON response
        // def parsedResponse = [:]
        // try {
        //     // Parse the response content
        //     def jsonSlurper = new JsonSlurper()
        //     parsedResponse = jsonSlurper.parseText(response)
        // } catch (e) {
        //     echo "Non-JSON response or parse error: ${e.message}"
        // }
        // return parsedResponse

        def parsedTeamsResponse = new JsonSlurper().parseText(response.content) as HashMap
        return parsedTeamsResponse
    }

    /**
    * Builds the final message payload to be sent to Microsoft Teams via webhook.
    * This wraps the Adaptive Card content along with team/channel/thread metadata.
    * Returns a structured payload ready to be sent to Teams via webhook.
    * @param type            Message type: 'post' or 'reply' (controls styling and placement).
        * @param teamsTeamName   Target Teams team name.
        * @param teamsChannelName Target Teams channel name.
        * @param threadId        (Optional) Thread ID for root post.
        * @param replyId         (Optional) Reply ID for nested replies.
        * @param status          Status indicator: 'loading', 'success', or 'failure' (controls icon).
        * @param msgTitle        Title text to display in the card.
        * @param msgBody         Detailed message body.
        * @return                A Map representing the Teams-compatible Adaptive Card payload.
        *
    */
    static Map buildTeamsMessagePayloadWithAdaptiveCard(String type, String teamsTeamName, String teamsChannelName, String threadId, String replyId, String status, String msgTitle, String msgBody) 
    {

        def adaptiveCardAttachment = constructAdaptiveCardPayloadForTeamsPost(type, status, msgTitle, msgBody)

        return [
            teamsChannelName: teamsChannelName,
            teamsTeamName   : teamsTeamName,
            type            : type,
            threadId        : threadId,
            replyId         : replyId,
            attachments     : [ adaptiveCardAttachment ]
        ]
    }

    /**
    * Constructs a Microsoft Teams Adaptive Card payload for either a new post or a reply message.
    *
    * Notes:
    * - Dynamically sets the icon and card styling based on status and message type.

    */

    static Map constructAdaptiveCardPayloadForTeamsPost(String type, String status, String msgTitle, String msgBody) 
    {

        String iconurl = 'https://i.gifer.com/ZKZg.gif'
        String containerStyle = ''
        String textSize = ''
        String fontColor = ''

        // Set icon URL based on status
        switch (status) {
        case 'loading':
                iconurl = 'https://i.gifer.com/ZKZg.gif'
                break
        case 'success':
                iconurl = 'https://cdn-icons-png.flaticon.com/512/845/845646.png'
                break
        case 'failure':
                iconurl = 'https://cdn-icons-png.flaticon.com/512/1828/1828665.png'
                break
        }

        // Set styling based on post type
        switch (type) {
        case 'post':
                containerStyle = 'default'
                textSize = 'large'
                fontColor = 'good'
                break
        case 'reply':
                containerStyle = 'accent'
                textSize = 'medium'
                fontColor = 'default'
                break
        }

        return [
            contentType: 'application/vnd.microsoft.card.adaptive',
            content: [
                type    : 'AdaptiveCard',
                $schema : 'http://adaptivecards.io/schemas/adaptive-card.json',
                version : '1.4',
                msTeams : [ width: 'Full' ],
                body    : [
                    [
                        type : 'Container',
                        style: containerStyle,
                        bleed: true,
                        items: [
                            [
                                type   : 'ColumnSet',
                                columns: [
                                    [
                                        type : 'Column',
                                        width: 'auto',
                                        items: [
                                            [
                                                type : 'Image',
                                                url  : iconurl,
                                                size : 'medium',
                                                width: '20px',
                                                style: 'default'
                                            ]
                                        ]
                                    ],
                                    [
                                        type : 'Column',
                                        width: 'stretch',
                                        items: [
                                            [
                                                type : 'TextBlock',
                                                text : msgTitle,
                                                weight: 'bolder',
                                                size : textSize,
                                                wrap : true,
                                                color: fontColor
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ],
                    [
                        type : 'Container',
                        items: [
                            [
                                type   : 'ColumnSet',
                                columns: [
                                    [
                                        type : 'Column',
                                        items: [
                                            [
                                                type : 'TextBlock',
                                                text : msgBody,
                                                weight: 'bolder',
                                                size : 'default',
                                                color: 'default',
                                                wrap : true
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
    }
}
