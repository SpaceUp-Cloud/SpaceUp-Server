const loaderElement =
    $("<div class=\"sk-folding-cube\">" +
        "<div class=\"sk-cube1 sk-cube\"></div>" +
        "<div class=\"sk-cube2 sk-cube\"></div>" +
        "<div class=\"sk-cube4 sk-cube\"></div>" +
        "<div class=\"sk-cube3 sk-cube\"></div>" +
    "</div>")

function enableLoader(name) {
    // Get original content
    let content = $("#content_" + name)
    let loader = loaderElement.clone();

    // ... and save it
    localStorage.setItem("content_" + name, content.html());

    // use loader element
    content.children().remove();
    content.append(loader);
}

function disableLoader(name) {
    let content = $("#content_" + name);
    let getOriginalContent = localStorage.getItem("content_" + name);

    // Remove loader
    content.children().remove();
    content.append(getOriginalContent);
}
