$(document).ready(function () {
    $('.card .btn').on('click', function (event) {
        event.preventDefault()
        let href = $(this).attr('href')
        jQuery.get(href, function (item) {
            $('#itemId').val(Number(item.id))
            $('.modal #priceTag').text(Number(item.price1day).toFixed(2))
            $('#itemPrice').val(Number(item.price1day).toFixed(2))
            let btn1 = document.getElementById('btn1day');
            if (item.price1day == null){
                $(btn1).attr('disabled', true)
            } else {
                $(btn1).attr('disabled', false)
                btn1.value = (Number(item.price1day).toFixed(2))
            }
            let btn3 = document.getElementById('btn3days')
            if (item.price3days == null){
                $(btn3).attr('disabled', true)
            } else {
                $(btn3).attr('disabled', false)
                btn3.value = (Number(item.price3days).toFixed(2))
            }
            let btn7 = document.getElementById('btn7days')
            if (item.price7days == null){
                $(btn7).attr('disabled', true)
            } else {
                $(btn7).attr('disabled', false)
                btn7.value = (Number(item.price7days).toFixed(2))
            }
            let btn365 = document.getElementById('btn1year')
            if (item.price1year == null){
                $(btn365).attr('disabled', true)
            } else {
                $(btn365).attr('disabled', false)
                btn365.value = (Number(item.price1year).toFixed(2))
            }
        })

        $('#myOrderModal').modal()
    })
})

function updatePrice(clickedId) {
    let button = document.getElementById(clickedId)
    let newPrice = button.value
    $(button).attr('active', true)
    $('#priceTag').text(newPrice)
    $('#itemPrice').val(newPrice)
}

function validateMail(e1, e2) {
    if (e1.value != e2.value || e2.value == ''){
        e2.setCustomValidity('Oba pola muszą się zgadzać')
    } else {
        e2.setCustomValidity('')
    }
}