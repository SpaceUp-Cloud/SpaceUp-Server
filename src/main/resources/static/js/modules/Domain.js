/**
 * Helper class to handle SSE for domain specific messages
 */
export default class SseDomainHandler {

    constructor() {}

    /**
     * Get a response from SSE and handle it
     * @param feedback - object
     */
    handleDomainAdd(feedback) {
        console.debug("SSE add domain: " + feedback)
        popup(JSON.parse(feedback))
    }

    /**
     * Handle the response from SSE when a domain will be deleted
     * @param feedback - object
     */
    handleDomainDelete(feedback) {
        console.debug("SSE delete domain: " + feedback)
        popup(JSON.parse(feedback));
    }
}

/**
 * Add a new domain (if it belongs to you) to your uberspace
 */
export function add() {
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
    });
}

/**
 * Delete a specific domain from uberspace
 * @param domain - domain name
 */
export function deleteDomain(domain) {
    let c = confirm("Sicher das du die Domain '" + domain + "' löschen magst?");
    if(c === true) {
        let domainId = domain.replaceAll(".", "_");
        console.warn("Delete domain " + domain);

        $.ajax({
            url: "/api/domain/delete/" + domain,
            method: "Delete",
            beforeSend: function () {
                enableLoader(domainId)
            },
            complete: function () {
                disableLoader(domainId)
                markAsDelete("***REMOVED***content_" + domainId)
            }
        }).done(function(response) {

        });
    }
}

