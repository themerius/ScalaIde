/**
* This script loads the editor and maps function
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license BSD common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Editor = function($){

  if ( jQuery('#editor').length == 0 ) {
      //no need to initialize
      return false;
  }

  /**
  * Contains the filename
  *
  * @var
  * @access public
  * @type string
  */
  this._fileName = "";
  
  /**
   * Scope duplicator / parent this
   *
   * @var
   * @access private
   * @type object
   */
   var that = this;
  
  /**
  * Constructor
  *
  * Initializes the editor
  *
  * @access public
  * @return void
  */
  this.init = function() {
    $("#editor").keyup(this.handleKey);
  };

  this.handleKey = function(e) {
    var msg = {
      "type": "editor",
      "command": "save",
      "file": that._fileName,
      "value": window.aceEditor.getSession().getValue()
    };
    IDE.htwg.websocket.sendMessage( msg );
  };

  //probably there might be more commands
  this.executeCommand = function(data){
    switch ( data.command ){
      case "load":
        this.loadSourceFile(data);
        break;
      case "remove":
        this.closeTab(data.value);
        break;
      default:break;
    }
  };
  
  this.loadSourceFile = function(data){
    window.aceEditor.getSession().setValue(data.text);
    this._fileName = data.filename;
    
    if ( this._fileName == "" || typeof this._fileName === "undefined" ){
      return;
    }
    $("#editorTabs").show();
    $("#editor").css("top", $("#editorTabs").css("height"));
    
    cleanedFileName = this._fileName.replace(/\\/g,"/"); 
    
    openSourceFile = $("#editorTabs").find('span[title="'+ cleanedFileName +'"]')
    
    if ( openSourceFile.length === 0 )
    {
      shortFileName = this._fileName.substring( this._fileName.lastIndexOf("\\") + 1, this._fileName.length );
      tab = $('<span class="tab open" title="'+ cleanedFileName +'"><a href="#" class="close"></a>' + shortFileName + '</span>');
      tab.click( function ( ){
        var parentTab = $(this);
        targetSourceFile = $("#browser").find("li").filter(function () {
          var $el = $(this);
          return $el.attr("title") === parentTab.attr("title").replace(/\//g,"\\");
        });
        $("#browser").jstree("deselect_all"); 
        $("#browser").jstree("select_node", targetSourceFile); 
      });
      
      $("#editorTabs").append(tab);
    }
    else{
      openSourceFile.addClass("open");
    }
  
    $("#editorTabs").find('span[title!="'+ cleanedFileName +'"]').each(function(i, elem){
      $(elem).removeClass("open");
    });
    
    document.title = cleanedFileName;
  };
  
  this.closeTab = function(files){
    jQuery.each(files, function(i, filename) {
      console.log(filename);
    });
  };

  this.init();    
};
