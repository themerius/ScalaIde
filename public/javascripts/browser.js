/**
* This script loads the project browser
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license ace common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Browser = function($){
	
	
	// TODO: 1. js auslagern CHECK
    //       2. bindings erweitern,
    //       3. statt per ajax alles an den websocket schicken
    //       4. befehle in scala mappen
    //       5. befehle in scala implementieren -> - file/ordner name ändern,
    //                                             - file/ordner anlegen,
    //                                             - file/ordner löschen,  
	
	
	this.initialize = function(){
		jQuery("#browser")
				.jstree({
					"plugins" : ["themes","html_data"],
					"core" : { "initially_open" : [ "phtml_1" ] }
				})

				.bind("loaded.jstree", function (event, data) {
				});

    };
	
	this.initialize();
};