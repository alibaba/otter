JSONFormatter = (function() {

  var init = function( json, options ) { 
    
    // default settings
    var settings = $.extend( {
      'appendTo' : 'body',
      'list_id' : 'json',
      'collapse' : false
    }, options);
    
    var loopCount = 0;
    
    loopObjectOfObjects = function(json2, ulId) {
      $.each(json2, function(k3, v3) {
        // object of objects
        if(typeof v3 == 'object') {
          $('#' + settings.list_id + ' #' + ulId).append('<li><span>{</span> <ul id="' + ulId + '-' + k3 + '"></ul></li>');
          $.each(v3, function(k4, v4) {
            if(typeof v4 == 'object' && v4 != null) {
              $('#' + settings.list_id + ' #' + ulId + '-' + k3).append('<li>' + k4 + ' <span>{</span> <ul id="'+k4+'-'+loopCount+'"></ul></li>');
              loopAgain(v4, k4, k4 + '-' + loopCount);
            }
            else {
              $('#' + settings.list_id + ' #' + ulId + '-' + k3).append('<li>' + k4 + ': ' + v4 + '</li>');
            }

          });
        } 
        else {
          // normal array
          $('#' + settings.list_id + ' #' + ulId).append('<li>' + v3 + '</li>')
        }
      });
    },

    loopAgain = function(v, k, ulId) {
      loopCount++;
      $.each(v, function(nextKey, nextVal) {
        var nextListId = nextKey + '-' + loopCount;
        var newList = '<ul id="' + nextListId + '"></ul>';
        if(nextVal != null && typeof nextVal == 'object') {
          if(nextVal.length == 0) {
            // an empty object, just output that
            $('#' + settings.list_id + ' #' + ulId).append('<li>' + nextKey + ': []</li>');
          } 
          else if(nextVal.length >= 1) {
            // an object of objects
            $('#' + settings.list_id + ' #' + ulId).append('<li><b>' + nextKey + ':</b> <span>[</span> ' + newList + '</li>');
            loopObjectOfObjects(nextVal, nextListId);
          }
          else if(nextVal.length == undefined) {
            // next node
            $('#' + settings.list_id + ' #' + ulId).append('<li><b>' + nextKey + ':</b> <span>{</span> ' + newList + '</li>');
            loopAgain(nextVal, nextKey, nextListId);
          }        
        }
        else {
          // value|key
          // if(nextKey.val == undefined) {
          //   $('#' + settings.list_id + ' #' + ulId).append('<li>' + nextVal + '</li>');
          //   
          // }
          // else {
            $('#' + settings.list_id + ' #' + ulId).append('<li>'+ nextKey + ': ' + nextVal + '</li>');
            
          // }
        }
      });
    },
    
    addClosingBraces = function() {
      $('#' + settings.list_id + ' span').each(function() {
        var closingBrace = '<span>}</span>';
        if($(this).text() == "[") {
          closingBrace = '<span>]</span>';
        }
        $(this).parent().find('ul').eq(0).after(closingBrace);
      });      
    };

    var jsonList = $('<ul id="' + settings.list_id + '" />');

    $(settings.appendTo).append(jsonList);

    $.each(json, function(key, val) {
      
      
      
      if(val != null && typeof val == 'object') {
        var goObj = false;
        var goArray = false;
        var nk = '';
        $.each(val, function(nextKey, nextVal) {
        
          if(nextVal != null && typeof nextVal == 'object') {
            if(nextVal.length == undefined) {
              goObj = true;
              nk = nextKey;
            }
            else {
              goObj = false;
            }
          }
          else {
            // console.log('nextVal ' + nextVal);
            goArray = true;
          }
        });

        if(goObj) {
          $('#' + settings.list_id).append('<li><b>' + key + ':</b> <span>[</span><ul id="' + nk + '-' + loopCount + '"></ul></li>');
          loopObjectOfObjects(val, nk + '-' + loopCount);
        }
        else if(goArray) {
          $('#' + settings.list_id).append('<li><b>' + key + ':</b> <span>[</span><ul id="' + nk + '-' + loopCount + '"></ul></li>');
          loopAgain(val, nk, nk + '-' + loopCount);
        }
        else {
          $('#' + settings.list_id).append('<li><b>' + key + ':</b> <span>{</span><ul id="' + key + '-' + loopCount + '"></ul></li>');
          loopAgain(val, key, key + '-' + loopCount);              
        }
        
      }
      else {
        $('#' + settings.list_id).append('<li>' + key + ': ' + val + '</li>');
      }
    });
    
    addClosingBraces();
    
    if(settings.collapse) {
      addToggles(settings.list_id);      
    }
    
  },
  
  addToggles = function( listId ) {
    $('#' + listId + " > li").find('ul').each(function() {
      $(this).parent().find('span').eq(0).after('<span class="toggle fake-link"> - </span>');
    });

    $('#' + listId +' .toggle').next().slideUp().end().text(' + ');

    $('#' + listId +' .toggle').live('click', function() {
      if($(this).next().is(":visible")) {
        $(this).next().slideUp().end().text(' + ');
      }
      else {
        $(this).next().slideDown().end().text(' - ');
      }
    });
  };
  
  return {

    format: function(json, options) {
      init(json, options);
    }

  }
  

})();