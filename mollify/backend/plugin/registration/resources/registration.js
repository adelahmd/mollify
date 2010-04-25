/**
	Copyright (c) 2008- Samuli J�rvel�

	All rights reserved. This program and the accompanying materials
	are made available under the terms of the Eclipse Public License v1.0
	which accompanies this distribution, and is available at
	http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
	this entire header must remain intact.
*/

var session = null;
var preRequestCallback = null;
var postRequestCallback = null;
var protocolVersion = "1_5_0";

$(document).ready(function() {
	getSessionInfo(onSession, onServerError);				
});

function onSession(session) {
	if (!session.features["registration"] || !session["authentication_required"]) return;
	$("#register-button").click(onRegister);
	$("#registration-form").show();
}

function onRegister() {
	$(".registration-field").removeClass("invalid");
	var name = $("#username-field").val();
	var pw = $("#password-field").val();
	var confirmPw = $("#confirm-password-field").val();
	var email = $("#email-field").val();
	
	if (name.length == 0) $("#username-field").addClass("invalid");
	if (pw.length == 0) $("#password-field").addClass("invalid");
	if (confirmPw.length == 0) $("#confirm-password-field").addClass("invalid");
	if (email.length == 0) $("#email-field").addClass("invalid");
	if (name.length == 0 || pw.length == 0 || confirmPw.length == 0 || email.length == 0) return;
	
	if (pw != confirmPw) {
		$("#password-field").addClass("invalid");
		$("#confirm-password-field").addClass("invalid");
		return;
	}
	
	alert(name);
}

function onServerError(error) {
	var errorHtml = $.template("<div class='error'><div class='title'>${title}</div><div class='details'>${details}</div><div id='error-info'><div id='error-info-title'>Details</div><div id='error-info-content'>${info}</div></div></div>");
	$("body").html(errorHtml, {title: error.error, details: error.details, info: (error.trace ? error.trace : '' ) });
	
	if (!error.trace) {
		$('#error-info').hide();
	} else {
		$('#error-info-content').hide();
		$('#error-info-title').click(function(){ $('#error-info-title').toggleClass("open"); $('#error-info-content').slideToggle(); });
	}
}

function getSession() {
	return session;
}

function getSessionInfo(success, fail) {
	request("GET", 'session/info/'+protocolVersion, success, fail);
}

function register(name, pw, email, success, fail) {
	var data = JSON.stringify({name:name, password:generate_md5(pw), email:email});
	request("POST", 'registration', success, fail, data);
}

function request(type, url, success, fail, data) {
	if (preRequestCallback) preRequestCallback();
	
	var t = type;
	if (getSession() != null && getSession().features["limited_http_methods"]) {
		if (t == 'PUT' || t == 'DELETE') t = 'POST';
	}

	$.ajax({
		type: t,
		url: "r.php/"+url,
		data: data,
		dataType: "json",
		success: function(result) {
			if (postRequestCallback) postRequestCallback();
			success(result.result);
		},
		error: function (xhr, desc, exc) {
			if (postRequestCallback) postRequestCallback();
			
			var e = xhr.responseText;
			if (!e) fail({code:999, error:"Unknown error", details:"Request failed, no response received"});
			else if (e.substr(0, 1) != "{") fail({code:999, error:"Unknown error", details:"Invalid response received: " + e});
			else fail(JSON.parse(e));
		},
		beforeSend: function (xhr) {
			xhr.setRequestHeader("mollify-http-method", type);
		}
	});
}