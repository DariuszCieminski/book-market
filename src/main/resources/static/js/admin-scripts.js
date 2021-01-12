let userTableEditor;
let roleTableEditor;
let roleList = [];
let genreTableEditor;

function loadRoles() {
    //fetch roles from server and save them to the variable
    $.getJSON("/admin/roles").done(function (data) {
        $.each(data, function (key, value) {
            delete value["users"];
            roleList.push(value);
        });

        //prepare and assign roles to editor field
        userTableEditor.field("roles").update(prepareRoleList(roleList));
    });
}

function prepareRoleList(list) {
    let roles = [];

    $.each(list, function (key, val) {
        let role = {};
        role.label = val.name;
        role.value = val.id;
        roles.push(role);
    });

    return roles;
}

function prepareServerData(data) {
    data = jsonPath(data, "$.data.*")[0];

    //return here if we don't modify User
    if (!data.hasOwnProperty("roles")) {
        return JSON.stringify(data);
    }

    //include user roles as full json object
    data["roles"] = roleList.filter(function (element) {
        return data["roles"].includes(element["id"]);
    });

    return JSON.stringify(data);
}