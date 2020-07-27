$(document).ready(function () {
    $('a[data-confirm]').click(function (event) {
        let itemId = $(this).attr('data-item-id')
        let modalHTML =
            $('<div id="deleteConfirmModal" class="modal fade" role="dialog" aria-labelledby="deleteConfirmLabel" aria-hidden="true">' +
                '<div class="modal-dialog">' +
                    '<div class="modal-content">' +
                        '<div class="modal-header">' +
                            '<h3 id="deleteConfirmLabel">Potwierdzenie</h3> ' +
                            '<button type="button" class="close" data-dismiss="modal" aria-label="Close">' +
                               '<span aria-hidden="true">×</span>' +
                            '</button>' +
                        '</div>' +
                        '<div class="modal-body"></div>' +
                        '<div class="modal-footer">' +
                            '<button class="btn" data-dismiss="modal" aria-hidden="true">Anuluj</button>' +
                            '<form method="post" action="/admin/removeItem?id=' + itemId + '">' +
                                '<button class="btn btn-outline-danger" type="submit" id="deleteConfirmOK">Usuń</button>' +
                            '</form>' +
                        '</div>' +
                    '</div> ' +
                '</div> ' +
            '</div>')

        if (!$('#deleteConfirmModal').length) {
            $('body').append(modalHTML)
        }
        $('#deleteConfirmModal').find('.modal-body').text($(this).attr('data-confirm'))
        $('#deleteConfirmModal').modal({show:true})
        return false
    })

})

$(document).ready(function () {
    console.log($('#itemsTable'))
    $('#itemsTable, #ordersTable').DataTable({
        "scrollX": true,
        "scrollY": 400,
        "searching" : true,

        initComplete: function () {
            this.api().columns().every( function () {
                var column = this;
                var search = $(`<input class="form-control form-control-sm" type="text" placeholder="Search">`)
                    .appendTo( $(column.footer()).empty() )
                    .on( 'change input', function () {
                        var val = $(this).val()

                        column
                            .search( val ? val : '', true, false )
                            .draw();
                    } );

            } );
        }
    });
});