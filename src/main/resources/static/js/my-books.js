let genreList = [];

function loadGenres(editor) {
    $.getJSON("/api/books/genres").done(function (data) {
        genreList = data;
        let genres = [];

        //prepare data and update editor field
        for (let i = 0; i < data.length; i++) {
            let option = {};
            option.value = data[i]["id"];
            option.label = data[i]["name"];
            genres.push(option);
        }
        editor.field("genre").update(genres);
    });
}

function loadToolTips(element) {
    element.not('.tooltipstered').tooltipster({
        theme: 'tooltipster-shadow',
        delay: 100
    });
}

function prepareServerData(data) {
    data = jsonPath(data, "$.data.*")[0];

    //assign full json object as genre
    data["genre"] = genreList.find(function (element) {
        return element["id"] === data["genre"];
    });

    return JSON.stringify(data);
}