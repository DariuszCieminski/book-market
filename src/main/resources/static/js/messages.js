function renderMessage(data) {
    data = translateMsg(data);
    let message = data.split("|");

    //if offer message contains additional comment, render it as itallic
    if (message.length > 1) {
        message[1] = '<i style="font-size: small">' + message[1] + '</i>';
        data = message.join('<br>');
    }

    return data;
}

//after opening messages view set them to read
function setMessagesRead(messages) {
    let unread = messages.filter(function (element) {
        return element.read === false;
    });

    //map message object to its ID
    unread = unread.map(function (element) {
        return element.id;
    });

    //send IDs of unread messages
    if (unread.length > 0) {
        $.ajax({
            headers: {"X-CSRF-TOKEN": $("meta[name='_csrf']").attr("content")},
            url: "/api/messages",
            method: "PUT",
            data: {ids: unread},
            traditional: true
        });
    }
}