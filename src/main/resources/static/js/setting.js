$(function(){
    $("#password-from").submit(check_data);
    $("input").focus(clear_error);
});

function check_data() {
    var pwd1 = $("#new-password").val();
    var pwd2 = $("#confirm-password").val();
    var pwd3 = $("#old-password").val();
    if(pwd1 != pwd2) {
        $("#confirm-password").addClass("is-invalid");
        return false;
    }
    if(pwd1 == pwd3) {
        alert("原密码不能与新密码相同");
        return false;
    }
    $("#submitBtn").css("pointer-events","none")
    return true;
}

function clear_error() {
    $(this).removeClass("is-invalid");
}