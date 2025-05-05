import groovy.json.JsonBuilder

def call(String teamsWebhookUrl,
         String type,
         String teamsTeamName,
         String teamsChannelName,
         String threadId,
         String replyId,
         String status,
         String msgTitle,
         String msgBody) {

    def payload = buildTeamsMessagePayloadWithAdaptiveCard(
        type, teamsTeamName, teamsChannelName,
        threadId, replyId, status, msgTitle, msgBody
    )

    def jsonPayload = new JsonBuilder(payload).toPrettyString()

    def response = httpRequest(
        url: teamsWebhookUrl,
        httpMode: 'POST',
        contentType: 'APPLICATION_JSON',
        requestBody: jsonPayload,
        validResponseCodes: '200:299',
        consoleLogResponseBody: true
    )

    return response
}

def buildTeamsMessagePayloadWithAdaptiveCard(String type,
                                             String teamsTeamName,
                                             String teamsChannelName,
                                             String threadId,
                                             String replyId,
                                             String status,
                                             String msgTitle,
                                             String msgBody) {
    def adaptiveCardAttachment = constructAdaptiveCardPayloadForTeamsPost(
        type, status, msgTitle, msgBody
    )

    return [
        teamsChannelName: teamsChannelName,
        teamsTeamName   : teamsTeamName,
        type            : type,
        threadId        : threadId,
        replyId         : replyId,
        attachments     : [ adaptiveCardAttachment ]
    ]
}

def constructAdaptiveCardPayloadForTeamsPost(String type,
                                             String status,
                                             String msgTitle,
                                             String msgBody) {
    def iconurl = 'https://i.gifer.com/ZKZg.gif'
    def containerStyle = 'default'
    def textSize = 'medium'
    def fontColor = 'default'

    switch (status) {
        case 'loading':
            iconurl = 'https://i.gifer.com/ZKZg.gif'; break
        case 'success':
            iconurl = 'https://cdn-icons-png.flaticon.com/512/845/845646.png'; break
        case 'failure':
            iconurl = 'https://cdn-icons-png.flaticon.com/512/1828/1828665.png'; break
    }

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
                    items: [[
                        type   : 'ColumnSet',
                        columns: [[
                            type : 'Column',
                            width: 'auto',
                            items: [[
                                type : 'Image',
                                url  : iconurl,
                                size : 'medium',
                                width: '20px',
                                style: 'default'
                            ]]
                        ],[
                            type : 'Column',
                            width: 'stretch',
                            items: [[
                                type : 'TextBlock',
                                text : msgTitle,
                                weight: 'bolder',
                                size : textSize,
                                wrap : true,
                                color: fontColor
                            ]]
                        ]]
                    ]]
                ],
                [
                    type : 'Container',
                    items: [[
                        type   : 'ColumnSet',
                        columns: [[
                            type : 'Column',
                            items: [[
                                type : 'TextBlock',
                                text : msgBody,
                                weight: 'bolder',
                                size : 'default',
                                color: 'default',
                                wrap : true
                            ]]
                        ]]
                    ]]
                ]
            ]
        ]
    ]
}

