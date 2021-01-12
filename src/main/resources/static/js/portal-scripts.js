function getCookie(name) {
    let cookie = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return cookie ? cookie[2] : null;
}

function getLanguage() {
    let lang = getCookie("lang");
    return lang == null ? "pl" : lang;
}

function changeLanguage() {
    $.ajax({
        headers: {"X-CSRF-TOKEN": $("meta[name='_csrf']").attr("content")},
        url: "/setlanguage",
        method: "post",
        data: {"lang": $("#select-lang").val()},
        success: function () {
            location.reload(true);
        }
    });
}

function openTab(event, tabID, isVertical) {
    let tabContent = document.getElementsByClassName(isVertical ? "tab-ver-content" : "tab-hor-content");
    for (let i = 0; i < tabContent.length; i++) {
        tabContent[i].style.display = "none";
    }

    let tabButtons = document.getElementsByClassName("tab-button");
    for (let i = 0; i < tabButtons.length; i++) {
        tabButtons[i].className = tabButtons[i].className.replace(" active", "");
    }

    document.getElementById(tabID).style.display = "block";
    event.currentTarget.className += " active";
}

function getMessages() {
    $.get("/api/messages/unread").done(function (data) {
        //if no unread messages, remove counter badge from the message button and open messages view after clicking it
        //otherwise show context menu with unread messages and create counter badge on the button
        if (data.length === 0) {
            $('#counter').remove();
            $('#message-btn').click(function () {
                location.href = '/messages';
            });
        } else {
            let comment_list = [];

            data.sort(function (a, b) {
                return a.sendTime < b.sendTime;
            });

            $.each(data, function (index, value) {
                comment_list.push({
                    text: formatMessage(value),
                    onclick: () => {
                        location.href = '/messages'
                    }
                });
            });

            $('#message-btn').click(function (e) {
                showContextMenu(e, comment_list, {
                    arrow: true,
                    align: 'center',
                    element: this,
                    width: '330px',
                    margin: {bottom: 10}
                });
            });

            $('#counter').text(data.length);
        }
    });
}

//format message to show in context menu
function formatMessage(msg) {
    let message = translateMsg(msg.text).split("|");
    let date = new Date(msg.sendTime);

    let html = '<p class="date">' + leadZero(date.getDate()) + '.' + leadZero(date.getMonth() + 1) + '.' + date.getFullYear()
               + ' ' + leadZero(date.getHours()) + ':' + leadZero(date.getMinutes()) + ' - ' + msg.sender.login + '</p>';
    html += '<p class="message">' + truncate(message[0], 80) + '</p>';

    if (message.length > 1) {
        html += '<h6 class="comment">' + truncate(message[1], 80) + '</h6>';
    }

    return html;
}

function translateMsg(msg) {
    let templates = msg.match(/{\S+}/g);

    $.each(templates, function (index, template) {
        translation.forEach(function (val, key) {
            if (template.includes(key)) {
                msg = msg.replace(template, val);
            }
        });
    });

    return msg;
}

function truncate(text, max) {
    if (text.length > max) {
        text = text.substr(0, max) + '...';
    }

    return text;
}

function leadZero(val) {
    return (val < 10) ? ("0" + val) : val;
}