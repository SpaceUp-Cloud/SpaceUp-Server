function add() {
    let rawDomains = $("***REMOVED***domain-list").val()

    // Validation
    if(rawDomains == null || rawDomains === "") {
        alert("Die Liste darf nicht leer sein!");
        return;
    }

    let urlList = rawDomains.split(";").map((val) => {
        return val.trim();
    });

    const domainList = [];
    urlList.forEach(d => {
        let domain = {
            url: d
        }

        domainList.push(domain);
    });

    const json = JSON.stringify(domainList);
    console.debug(json);

    $.ajax({
        url: "/api/domain/add",
        method: "POST",
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        data: json
    }).done(function(response) {
        console.debug(response);

        let tempRes = {};

        for (let domain in response) {
            if(domain == null) break;

            let err_info = response[domain];
            for(let i in err_info) {
                if(i != null && err_info[i] != null) {
                    err_info[i] = domain + ": " + err_info[i];
                }

                if(i === "info") {
                    info(err_info[i]);
                }

                if(i === "error") {
                    error(err_info[i]);
                }
            }
        }
    });
}

function deleteDomain(domain) {
    let c = confirm("Bist du dir sicher?");
    let domainId = domain.replaceAll(".", "_");

    if(c === true) {
        console.warn("Delete domain " + domain);
        $.ajax({
            url: "/api/domain/delete/" + domain,
            method: "Delete",
            beforeSend: function () {
                enableLoader(domainId)
            },
            complete: function () {
                disableLoader(domainId)
            }
        }).done(function(response) {
            console.debug(response);
            popup(response);

            let info = response["info"];
            if(response != null && info != null) {
                if(info.toLowerCase().includes("success"))
                // Card can be removed on client
                $("***REMOVED***card_" + domainId).remove();
            }
        });
    }
}
