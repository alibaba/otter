function isRunning(nid,pid,stage) {
	if(stage == 'SELECT') {
		Node.isSelectRunning(nid,pid,callback);  
	}else if(stage == 'EXTRACT'){
		Node.isExtractRunning(nid,pid,callback);  
	}else if(stage == 'TRANSFORM') {
		Node.isTransformRunning(nid,pid,callback);  
	}else {
		Node.isLoadRunning(nid,pid,callback);  
	}
}

function aggregation(nid,pid,stage) {
	if(stage == 'SELECT') {
		Node.selectStageAggregation(nid,pid,callback);  
	}else if(stage == 'EXTRACT'){
		Node.extractStageAggregation(nid,pid,callback);  
	}else if(stage == 'TRANSFORM') {
		Node.transformStageAggregation(nid,pid,callback);  
	}else {
		Node.loadStageAggregation(nid,pid,callback);  
	}
}

function pending(nid,pid,stage) {
	if(stage == 'SELECT') {
		Node.selectPendingProcess(nid,pid,callback);  
	}else if(stage == 'EXTRACT'){
		Node.extractPendingProcess(nid,pid,callback);  
	}else if(stage == 'TRANSFORM') {
		Node.transformPendingProcess(nid,pid,callback);  
	}else {
		Node.loadPendingProcess(nid,pid,callback);  
	}
}

function callback(msg) {
	alert(msg);
}