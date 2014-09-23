// JavaScript Document
// JavaScript Document
window.onload = function() {
 $('.changecolor_g tbody tr:even').addClass('odd');
 $('.changecolor_g tbody tr').hover(
  function() {$(this).addClass('highlight');},
  function() {$(this).removeClass('highlight');}
 );
 
 $('.changecolor_w tbody tr:even').addClass('wh');
 $('.changecolor_w tbody tr').hover(
  function() {$(this).addClass('highlight');},
  function() {$(this).removeClass('highlight');}
 );
}






