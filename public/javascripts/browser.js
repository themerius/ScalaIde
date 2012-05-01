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
	
	// TODO: 1. befehle in scala implementieren -> - file/ordner name ändern,
    //                                             - file/ordner anlegen,
    //                                             - file/ordner löschen,  
	
	
	this.initialize = function(){
		jQuery("#browser")
				.jstree({
					"plugins" : ["themes","html_data", "types", "ui", "contextmenu"],

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
		
				.bind("select_node.jstree", function (event, data) {
				     if ( data.rslt.obj.attr("rel") === "file" ){
				    	 
				    	 var msg = {
				    			 "command" : "load",
				    			 "file": data.rslt.obj.attr("target")					    			 
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
    			"action": function (obj) { that.renameItem(obj); }
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
    	alert("rename");
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