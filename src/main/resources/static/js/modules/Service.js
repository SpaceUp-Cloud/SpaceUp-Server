function serviceExecute(name, option) {
    let url = '/services/execute/' + name + '/' + option;
    $.ajax({
        url: url,
        method: 'post',
        beforeSend: function () {
            enableLoader(name)
        },
        complete: function () {
            disableLoader(name)
        }
    }).done(function (response) {
        M.toast({html: response, classes: 'blue'})
    })
}