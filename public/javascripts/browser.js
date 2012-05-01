/**
* This script loads the project browser
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license BSD license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Browser = function($){
	
	if ( jQuery('#browser').length == 0 ) {
	//no need to initialize
	    return false;
	}   
	
	/**
	 * Contains the webSocket
	 *
	 * @var
	 * @access public
	 * @type object
	 */
	 this._webSocket = false;
	
	/**
	* Scope duplicator / parent this
	*
	* @var
	* @access private
	* @type object
	*/
	var that = this;
	
	// TODO: 1. befehle in scala implementieren -> - file/ordner anlegen,
	//                                             - file/ordner löschen,  
	
	
	this.initialize = function(){
		jQuery("#browser")
			.jstree({
				"plugins" : ["themes","html_data", "types", "ui", "contextmenu", "crrm"],
	
		        "themes" : {
		        	"theme" : "classic"
		        },
		        "ui" : {
		        	"select_limit" : 1,
		        	"select_multiple_modifier" : "alt",
		        	"selected_parent_close" : "select_parent",
		        	"initially_select" : [ "root" ]
		        },
				"types" : {
					"valid_children" : [ "drive" ],
					"types" : {
						"file" : {
							"valid_children" : "none",
							"icon" : {
								"image" : "/assets/images/file.png"
							}
						},
						"root" : {
							"valid_children" : [ "default", "folder" ],
							"icon" : {
								"image" : "/assets/images/root.png"
							},
							"start_drag" : false,
							"move_node" : false,
							"delete_node" : false,
							"remove" : false
						}
					}
				},
				'contextmenu' : {
					  'items' : that.customMenu
				},
	
				"core" : { "initially_open" : [ "root" ] }
			})
	
			.bind("loaded.jstree", function (event, data) {
			})
			
			.bind("rename_node.jstree", function (event, data) {
			  
			  var regex = /^[a-zA-Z._ ]*$/;
			  if (!regex.test(data.rslt.name))
			  {
          alert("Characters not allowed.");
          $.jstree.rollback(data.rlbk);
          return;
			  }
			    
			  if ( data.rslt.obj.attr("rel") === "file" || data.rslt.obj.attr("rel") === "folder" ){
			    
			    var lastPositionOfSlash = data.rslt.obj.attr("title").lastIndexOf("\\");
			    var relativePath = data.rslt.obj.attr("title").substring(0, lastPositionOfSlash);
			    var newFileName = relativePath + "\\" + data.rslt.name;	    

			    var countDuplicateObjects = data.rslt.obj.siblings().filter(function () {
	          var $el = $(this);
	          return $el.attr("title") === newFileName &&
	                 ( $el.attr("rel") === "file" || $el.attr("rel") === "folder" );
			    }).length;
			      		      
			    if ( data.rslt.obj.attr("rel") === "file" && countDuplicateObjects > 0 ){
			      alert("File already exists!");
	          $.jstree.rollback(data.rlbk);
			      return;
			    }
			    
          if ( data.rslt.obj.attr("rel") === "folder" && countDuplicateObjects > 0 ){
            alert("Folder already exists!");
            $.jstree.rollback(data.rlbk);
            return;
          }
			    
          //changing all title attributes in subtree
          if ( data.rslt.obj.attr("rel") === "folder" ){
            var lengthOfOldFileName = data.rslt.obj.attr("title").length;
            
            data.rslt.obj.find('li').each(function(i, elem) {
              var postFix = $(elem).attr("title").substring(lengthOfOldFileName, $(elem).attr("title").length);
              $(elem).attr("title", newFileName + postFix ); 
            });
          }
          
          var msg = {
              "command" : "rename",
              "file": newFileName,
              "value": data.rslt.obj.attr("title"),
              "folder": false
            };
          
          if ( data.rslt.obj.attr("rel") === "folder" ){
            msg.folder = true;
          }
          
			    data.rslt.obj.attr("title", newFileName);
          IDE.htwg.editor.sendMessage( msg );
			  }
			})
	
			.bind("select_node.jstree", function (event, data) {
			     if ( data.rslt.obj.attr("rel") === "file" ){
			    	 
			    	 var msg = {
			    			 "command" : "load",
			    			 "file": data.rslt.obj.attr("title")					    			 
				    	 };
				    	 
				    	 IDE.htwg.editor.sendMessage( msg );
				     }
				}); 
	
	};
	
	this.customMenu = function(node){
		
	    // The default set of all items
	var items = {
	    renameItem: {
			"label": "Rename",
			"action": function (obj) {
			  if ( obj.attr("rel") !== "root" )
			    $("#browser").jstree("rename");
			  }
	    },
	    deleteItem: { // The "delete" menu item
	        label: "Delete",
	        action: function (obj) {
	        	if ($(this._get_node(obj)).hasClass("folder") ){
	        		return;
	        	}
	        	that.deleteItem(obj)
	        }
	    }
	};
	
	/*if ($(node).hasClass("folder")) {
	    // Delete the "delete" menu item
	    delete items.deleteItem;
	}*/
	
	    return items;
	}
	
	this.renameItem = function(obj){
		var msg = {
			 "command" : "load",
		 "file": data.rslt.obj.attr("title"),
		 "value": "test"
	    };
		IDE.htwg.editor.sendMessage( msg );
	}
	
	this.deleteItem = function(obj){
		alert("delete");
	}
	
	this.receiveEvent = function(event) {
	    //var data = JSON.parse(event.data)
	
	// Handle errors
	if(event.data.error) {
		chatSocket.close();
	    //$("#onError span").text(data.error)
	    //$("#onError").show()
	    alert("Error");
	    return;
	} else {
		window.aceEditor.getSession().setValue(event.data);
	    //$("#onChat").show()
	    }        
	};
	
	this.initialize();
};