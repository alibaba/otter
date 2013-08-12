 

function strFormat(s) {
    var flag;
    var arr = s.split(".");
    var l = arr[0].split("").reverse();
    var t = ""
    for (i = 0; i < l.length; i++) {
        t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
    }
    if (arr.length == 2) return t.split("").reverse().join("") + "." + arr[1];
    else return t.split("").reverse().join("");
} 

function sizeFormat(val, axis) { 
    if (val > 1073741824) 
      return (val / 1073741824).toFixed(axis.tickDecimals) + " TB"; 
    else if (val > 1048576) 
    	return (val / 1048576).toFixed(axis.tickDecimals) + " GB"; 
    else if (val > 1024)
    	return (val / 1024).toFixed(axis.tickDecimals) + " MB"; 
    else 
      return val.toFixed(axis.tickDecimals) + " KB"; 
}
    
    function drawDelayGraph(id,flotData) {
        var delayOptions = {
		legend: {
                        show: true,
                        position:"nw",
                },
        series: {
                    lines: { show: true },
                    points: { show: true }
                },
          grid: { hoverable: true, clickable: false },
                //colors:["#edc240","#cb4b4b","#4da74d","#9440ed"],
            xaxis: {
                tickFormatter: function(val, axis) {
                    return new Date(val).toTimeString().split(' ')[0];
                }
            },
            yaxis: {
                tickFormatter: function(val, axis) {
                    return strFormat(val.toFixed(axis.tickDecimals));
                }
            }
        };
        if (($(id).width() !== 0) && ($(id).height() !== 0)) {
            $.plot($(id), [{
                data: flotData
            }], delayOptions);
        }
    }


    function createDelayStatFlash(flotTime) {
        if (($("#delayTime").width() !== 0) && ($("#delayTime").height() !== 0)) {
            $.plot($("#delayTime"), [{
                data: [[0, 0]]
            }]);
        } 
        drawDelayGraph("#delayTime",flotTime);
    }
    
    function doPlot(position,flotNumber,flotTime) {
        $.plot($("#placeholder"),
           [ { data: flotTime, label: "延迟时间 (秒)" }],
           { 
               xaxes: [ { 
                   tickFormatter: function(val, axis) {
                      return new Date(val).toTimeString().split(' ')[0]; }} ],
               yaxes: [ 
                        { 
                            tickFormatter: function(val, axis) {
                               return val.toFixed(axis.tickDecimals) + '';}
                        },
                        {
                          // align if we are to the right
                          alignTicksWithAxis: position == "right" ? 1 : null,
                          position: position,
                          tickFormatter: function(val, axis) {
                              return val.toFixed(axis.tickDecimals);
                          }
                        } ],
		       legend: {
                        show: true,
                        position:"nw",
                       },
               series: {
                       lines: { show: true },
                       points: { show: true }
                       },
                 grid: { hoverable: true, clickable: false },
                       });
    }
    
    
    function createDelayStat(flotTime) {
        if (($("#placeholder").width() !== 0) && ($("#placeholder").height() !== 0)) {
            $.plot($("#placeholder"),
                    [ { data: [[0, 0]], label: "延迟时间 (秒)" }]);
        }
    	doPlot("right",flotTime);
    }
    
    function doPlot(id,position,flotNumber,flotSize) {
        $.plot($(id),
           [ { data: flotNumber, label: "number （条）" },
             { data: flotSize, label: "size (KB)", yaxis: 2 }],
           { 
               xaxes: [ { 
                   tickFormatter: function(val, axis) {
                      return new Date(val).toTimeString().split(' ')[0]; }} ],
               yaxes: [ 
                        { 
                            tickFormatter: function(val, axis) {
                               return strFormat(val.toFixed(axis.tickDecimals));}
                        },
                        {
                          // align if we are to the right
                          alignTicksWithAxis: position == "right" ? 1 : null,
                          position: position,
                          tickFormatter: function(val, axis) {
                              return strFormat(val.toFixed(axis.tickDecimals));
                          }
                        } ],
		       legend: {
                        show: true,
                        position:"nw",
                       },
               series: {
                       lines: { show: true },
                       points: { show: true }
                       },
                 grid: { hoverable: true, clickable: false },
                       });
    }
    
    
    function plotThroughput(id,flotData){
        var throughputOptions = {
        		legend: {
                                show: true,
                                position:"nw",
                        },
                series: {
                            lines: { show: true },
                            points: { show: true }
                        },
                  grid: { hoverable: true, clickable: false },
                        //colors:["#edc240","#cb4b4b","#4da74d","#9440ed"],
                    xaxis: {
                        tickFormatter: function(val, axis) {
                            return new Date(val).toTimeString().split(' ')[0];
                        }
                    },
                    yaxis: {
                        tickFormatter: function(val, axis) {
                            return strFormat(val.toFixed(axis.tickDecimals));
                        }
                    }
                };
                if (($(id).width() !== 0) && ($(id).height() !== 0)) {
                    $.plot($(id), [{
                        data: flotData
                    }], throughputOptions);
                }
    }
    
    function plotSizeThroughput(id,flotData){
        var throughputOptions = {
        		legend: {
                                show: true,
                                position:"nw",
                        },
                series: {
                            lines: { show: true },
                            points: { show: true }
                        },
                  grid: { hoverable: true, clickable: false },
                        //colors:["#edc240","#cb4b4b","#4da74d","#9440ed"],
                    xaxis: {
                        tickFormatter: function(val, axis) {
                            return new Date(val).toTimeString().split(' ')[0];
                        }
                    },
                    yaxis: {
                        tickFormatter: function(val, axis) {
                            return sizeFormat(val, axis);
                        }
                    }
                };
                if (($(id).width() !== 0) && ($(id).height() !== 0)) {
                    $.plot($(id), [{
                        data: flotData
                    }], throughputOptions);
                }
    }
    
    function createRowThroughputFlash(flotNumber,flotSize){
        if (($("#rowNumber").width() !== 0) && ($("#rowNumber").height() !== 0)) {
            $.plot($("#rowNumber"), [{
                label: "number",
                data: [[0, 0]]
            }]);
        }
        plotThroughput("#rowNumber",flotNumber);
        plotSizeThroughput("#rowSize",flotSize);
        
    }
    
    function createFileThroughputFlash(flotNumber,flotSize){
        if (($("#fileNumber").width() !== 0) && ($("#fileNumber").height() !== 0)) {
            $.plot($("#fileNumber"), [{
                label: "number",
                data: [[0, 0]]
            }]);
        }
        plotThroughput("#fileNumber",flotNumber);
        plotSizeThroughput("#fileSize",flotSize);
    }
    
    function drawBehaviorGraph(id,flotData) {
        var behaviorOptions = {
		legend: {
                        show: true,
                        position:"nw",
                },
        series: {
                    lines: { show: true },
                    points: { show: true }
                },
          grid: { hoverable: true, clickable: false },
                //colors:["#edc240","#cb4b4b","#4da74d","#9440ed"],
            xaxis: {
                tickFormatter: function(val, axis) {
                    return new Date(val).toTimeString().split(' ')[0];
                }
            },
            yaxis: {
                tickFormatter: function(val, axis) {
                    return strFormat(val.toFixed(axis.tickDecimals));
                }
            }
        };
        if (($(id).width() !== 0) && ($(id).height() !== 0)) {
            $.plot($(id), [{
                data: flotData
            }], behaviorOptions);
        }
    }


    function createBehaviorFlash(flotNumber1,flotNumber2,flotNumber3,flotNumber4,flotNumber5) {
        if (($("#insertNumber").width() !== 0) && ($("insertNumber").height() !== 0)) {
            $.plot($("#insertNumber"), [{
                data: [[0, 0]]
            }]);
        } 
        
        if (($("#updateNumber").width() !== 0) && ($("#updateNumber").height() !== 0)) {
            $.plot($("#updateNumber"), [{
                data: [[0, 0]]
            }]);
        } 
        
        if (($("#deleteNumber").width() !== 0) && ($("#deleteNumber").height() !== 0)) {
            $.plot($("#deleteNumber"), [{
                data: [[0, 0]]
            }]);
        } 
        
        if (($("#fileNumber").width() !== 0) && ($("#fileNumber").height() !== 0)) {
            $.plot($("#fileNumber"), [{
                data: [[0, 0]]
            }]);
        }
        
        if (($("#fileSize").width() !== 0) && ($("#fileSize").height() !== 0)) {
            $.plot($("#fileSize"), [{
                data: [[0, 0]]
            }]);
        }
        
        drawDelayGraph("#insertNumber",flotNumber1);
        drawDelayGraph("#updateNumber",flotNumber2);
        drawDelayGraph("#deleteNumber",flotNumber3);
        plotThroughput("#fileNumber",flotNumber4);
        plotSizeThroughput("#fileSize",flotNumber5);
    }
    
    function doPlot(position,flotNumber1,flotNumber2,flotNumber3,flotNumber4,flotNumber5) {
        $.plot($("#placeholder"),
           [ { data: flotNumber1, label: "insert （条）" },
             { data: flotNumber2, label: "update （条）", yaxis: 2 },
             { data: flotNumber3, label: "delete （条）", yaxis: 3 },
             { data: flotNumber3, label: "文件数量 （条）", yaxis: 4 },
             { data: flotNumber3, label: "文件大小 （KB）", yaxis: 5 }],
           { 
               xaxes: [ { 
                   tickFormatter: function(val, axis) {
                      return new Date(val).toTimeString().split(' ')[0]; }} ],
               yaxes: [ 
                        { 
                            tickFormatter: function(val, axis) {
                               return val.toFixed(axis.tickDecimals) + '';}
                        },
                        {
                          // align if we are to the right
                          alignTicksWithAxis: position == "right" ? 1 : null,
                          position: position,
                          tickFormatter: function(val, axis) {
                              return val.toFixed(axis.tickDecimals);
                          }
                        } ],
		       legend: {
                        show: true,
                        position:"nw",
                       },
               series: {
                       lines: { show: true },
                       points: { show: true }
                       },
                 grid: { hoverable: true, clickable: false },
                       });
    }
    
    
    function createBehaviorHistory(flotNumber1,flotNumber2,flotNumber3,flotNumber4,flotNumber5) {
        if (($("#placeholder").width() !== 0) && ($("#placeholder").height() !== 0)) {
            $.plot($("#placeholder"),
                    [ { data: [[0, 0]], label: "insert （条）" },
                      { data: [[0, 0]], label: "update （条）", yaxis: 2 },
                      { data: [[0, 0]], label: "delete （条）", yaxis: 3 },
                      { data: [[0, 0]], label: "文件数量 （条）", yaxis: 4 },
                      { data: [[0, 0]], label: "文件大小 （KB）", yaxis: 5 }]);
        }
    	doPlot("right",flotNumber1,flotNumber2,flotNumber3,flotNumber4,flotNumber5);
    }
    

