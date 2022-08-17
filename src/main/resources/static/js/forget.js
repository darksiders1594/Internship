$(function(){
	$("#verifyCodeBtn").click(getVerifyCode);
});

function getVerifyCode() {
    var email = $("#your-email").val();

    if(!email) {
        alert("还没有填写邮箱哦！");
        return false;
    }

	$.get(
	    CONTEXT_PATH + "/forget/code",
		{"email":email}

	);
	var $sendNewEmail=$("#verifyCodeBtn");
	time($sendNewEmail,30);
}

function time(o,wait) {
	var $this = $(o);
	if (wait == 0) {
		$this.css('pointer-events', '');
		$this.html("获取验证码");
		wait = 30;
	} else {
		$this.css("pointer-events","none");
		$this.html("重新发送(" + wait + ")");
		wait--;
		setTimeout(function() {
				time(o,wait)
			},
			1000)
	}
}