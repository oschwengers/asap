
$(document).ready(function(){
  $('body').append('<div id="toTop" class="btn btn-info"><i class="fa fa-arrow-up"></i>Back to Top</div>');
    $(window).scroll(function () {
        if ($(this).scrollTop() != 0) {
                $('#toTop').fadeIn();
        } else {
                $('#toTop').fadeOut();
        }
    });
    $('#toTop').click(function(){
        $("html, body").animate({ scrollTop: 0 }, 600);
        return false;
    });
});