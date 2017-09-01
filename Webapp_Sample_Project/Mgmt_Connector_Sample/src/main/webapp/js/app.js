$(function() {
	$("#register").click(function() {
		$.get("/management/rest/register", function(data) {
			window.location.replace(data);
		});
	});

	$("#deregister").click(function() {
		$.get("/management/rest/deregister", function(data) {
			window.location.replace(data);
		});
	});

	$.get("/management/rest/status", function(data) {
		$("#registrationStatus").text(data.status);
	});
});