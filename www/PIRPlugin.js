var pirPlugin = {
    pir: function() {
        cordova.exec(
			function(result) {
        		alert("success"+result);
             }, 
        	function(err){
                alert("err "+err);
            }, 
        	"PIRPlugin", //service
        	"ioioStartup", //action
            [{                  // and this array of custom arguments to create our entry
                "title": "1234"
            }]
        ); 
     }
}

