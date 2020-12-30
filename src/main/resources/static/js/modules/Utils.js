const loaderElement =
    $("<div class=\"sk-folding-cube\">" +
        "<div class=\"sk-cube1 sk-cube\"></div>" +
        "<div class=\"sk-cube2 sk-cube\"></div>" +
        "<div class=\"sk-cube4 sk-cube\"></div>" +
        "<div class=\"sk-cube3 sk-cube\"></div>" +
    "</div>")

function enableLoader(name) {
    // Get original content
    let content = $("***REMOVED***content_" + name)
    let loader = loaderElement.clone();

    // ... and save it
    localStorage.setItem("content_" + name, content.html());

    // use loader element
    content.children().remove();
    content.append(loader);
}

function disableLoader(name) {
    let content = $("***REMOVED***content_" + name);
    let getOriginalContent = localStorage.getItem("content_" + name);

    // Remove loader
    content.children().remove();
    content.append(getOriginalContent);
}

function popup(response) {
    if(response["info"] == null && response["error"] == null) {
        defaultMsg(response)
    } else {
        info(response["info"])
        error(response["error"])
    }
}

function info(data) {
    if(data != null) {
        M.toast({html: data, classes: "blue"});
    }
}

function error(data) {
    if(data != null) {
        M.toast({html: data, classes: "red"})
    }
}

function defaultMsg(data) {
    M.toast({html: data, classes: "grey"})
}

function markAsDelete(querySelector) {
    console.info("Mark "+ querySelector + " as deleted.")
    if(querySelector != null) {
        $(querySelector).addClass("deleted-overlay")
    } else {
        console.error("Query selector is null(..). Could not mark as delete.")
    }
}