function check() {  
	var sourceUrl = document.getElementById('sourceUrl').value;
    var sourceUserName = document.getElementById('sourceUserName').value;
    var sourcePassword = document.getElementById('sourcePassword').value;
    var sourceEncode = document.getElementById('sourceEncode').value;
    var sourceType = document.getElementById('sourceType').value;
    Hello.check(sourceUrl, sourceUserName, sourcePassword, sourceEncode, sourceType, callback);  
}  

function checkMap() {  
    var namespace = document.getElementById('namespace').value;
    var name = document.getElementById('name').value;
    var dataSourceId = document.getElementById('dataSourceId').value;
    
    Hello.checkMap(namespace, name, dataSourceId, callback);  
}
  
function callback(msg) {  
    DWRUtil.setValue('result', msg);  
}

function checkNamespaceTables() {  
    var namespace = document.getElementById('namespace').value;
    var name = document.getElementById('name').value;
    var dataSourceId = document.getElementById('dataSourceId').value;
    
    Hello.checkNamespaceTables(namespace, name, dataSourceId, callback2);  
}
  
function callback2(msg) {  
    var result = document.getElementById('result');
    if (/.*Find schema.*/.test(msg)) {
        result.innerHTML = "<font style='color:color;'>" + msg + "</font>";
    } else {
        result.innerHTML = msg;
    }
}

function changeform(){
}
