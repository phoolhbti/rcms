/**
 * SlingAsset extension to browse and upload assets to the JCR, then to insert
 * selected images into the Summernote HTML5 rich text editor. This pulls in the
 * /libs/rpgm/components/assetList component.
 */
(function (factory) {
  /* global define */
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);
  } else {
    // Browser globals: jQuery
    factory(window.jQuery);
  }
}(function($) {
  // template
  var tmpl = $.summernote.renderer.getTemplate();

  // core functions: range, dom
  var range = $.summernote.core.range;
  var dom = $.summernote.core.dom;
  var first = true;

  /**
   * createImageNode
   *  
   * @member plugin.slingasset
   * @private
   * @param {String} url
   * @return {Node}
   */
  var createImageNode = function (url) {
    var $img;

    if (url) {
      $img = $('<img>').attr('src', url);
      return $img[0];
    } else {
      return null;
    }
  };

  /**
   * @member plugin.slingasset
   * @private
   * @param {jQuery} $editable
   * @return {String}
   */
  var getTextOnRange = function ($editable) {
    $editable.focus();

    var rng = range.create();

    // if range on anchor, expand range with anchor
    if (rng.isOnAnchor()) {
      var anchor = dom.ancestor(rng.sc, dom.isAnchor);
      rng = range.createFromNode(anchor);
    }

    return rng.toString();
  };

  /**
   * Show Sling Asset dialog and set event handlers on dialog controls.
   *
   * @member plugin.slingasset
   * @private
   * @param {jQuery} $dialog
   * @param {jQuery} $dialog
   * @param {Object} text
   * @return {Promise}
   */
  var showAssetDialog = function ($editable, $dialog, text) {
    return $.Deferred(function (deferred) {
      var $assetDialog = $dialog.find('.note-slingasset-dialog');

      var $assetUrl = $assetDialog.find('.selected-asset'),
          $assetBtn = $assetDialog.find('.note-slingasset-btn');

      $assetDialog.one('shown.bs.modal', function () {
        $assetBtn.click(function (event) {
          event.preventDefault();

          deferred.resolve($assetUrl.val());
          $assetDialog.modal('hide');
        });
      }).one('hidden.bs.modal', function () {
        $assetUrl.off('input');
        $assetBtn.off('click');

        if (deferred.state() === 'pending') {
          deferred.reject();
        }
      }).modal('show');
    });
  };

  /**
   * @class plugin.slingasset
   *
   * SlingAsset Plugin
   *
   * Sling Asset plugin allows the user to browse the JCR
   * and insert an image tag.
   *
   * ### load script
   *
   * ```
   * <script src="plugin/summernote-ext-slingasset.js"></script>
   * ```
   *
   * ### use a plugin in toolbar
   * ```
   *    $("#editor").summernote({
   *    ...
   *    toolbar : [
   *        ['group', [ 'slingasset' ]]
   *    ]
   *    ...    
   *    });
   * ```
   */
  $.summernote.addPlugin({
    /** @property {String} name name of plugin */
    name: 'slingasset',
    /**
     * @property {Object} buttons
     * @property {function(object): string} buttons.slingasset
     */
    buttons: {
      slingasset: function (lang, options) {
        return tmpl.iconButton(options.iconPrefix + 'picture-o', {
          event: 'showAssetDialog',
          title: lang.asset.asset,
          hide: true
        });
      }
    },

    /**
     * @property {Object} dialogs
     * @property {function(object, object): string} dialogs.slingasset
    */
    dialogs: {
      slingasset: function (lang) {
        var body = $.ajax({
                      type: 'GET',
                      url: '/bin/admin/getassetlist',
                      cache: false,
                      async: false
                    }).responseText;

        var footer = '<button href="#" class="btn btn-primary note-slingasset-btn">' + lang.asset.insert + '</button>';
        return tmpl.dialog('note-slingasset-dialog', lang.asset.insert, body, footer);
      }
    },

    /**
     * @property {Object} events
     * @property {Function} events.showAssetDialog
     */
    events: {
      showAssetDialog: function (event, editor, layoutInfo) {
        var $dialog = layoutInfo.dialog(),
            $editable = layoutInfo.editable(),
            text = getTextOnRange($editable),
            $body = $('.asset-controller');

        // save current range
        editor.saveRange($editable);

        showAssetDialog($editable, $dialog, text).then(function (url) {
          // when ok button clicked

          // restore range
          editor.restoreRange($editable);
          
          // build node
          var $node = createImageNode(url);
          
          if ($node) {
            // insert asset node
            editor.insertNode($editable, $node);
          }
        }).fail(function () {
          // when cancel button clicked
          editor.restoreRange($editable);
        });

        if (first) {
          first = false;
          angular.element(document.body).injector().invoke(function($compile) {
            var scope = angular.element($body).scope();
            $compile($body)(scope);
          });
        }
      }
    },

    // define language
    langs: {
      'en-US': {
        asset: {
          asset: 'Asset',
          insert: 'Insert Asset'
        }
      }
    }
  });
}));
/**
 * Gist embed extension to the Summernote HTML5 WYSIWYG editor. Creates a CODE
 * tag with data-gist-* properties for the gist-embed jQuery plugin to convert
 * to an embedded Gist. See https://github.com/blairvanderhoof/gist-embed. The
 * Summernote display uses CSS to make sure the code tag is displayed in the
 * editor using the CSS attr() method.
 */
(function (factory) {
  /* global define */
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);
  } else {
    // Browser globals: jQuery
    factory(window.jQuery);
  }
}(function($) {
  // template
  var tmpl = $.summernote.renderer.getTemplate();

  // core functions: range, dom
  var range = $.summernote.core.range;
  var dom = $.summernote.core.dom;
  var first = true;

  /**
   * Create the code HTML element
   *
   * @member plugin.gistembed
   * @private
   * @param {String} gistId
   * @param {String} gistFile
   * @param {String} gistLine
   * @param {String} gistHighlightLine
   * @param {String} gistHideLineNumbers
   * @param {String} gistHideFooter
   * @param {String} gistShowLoading
   * @param {String} gistShowSpinner
   * @return {Node}
   */
  var createCodeNode = function (gistId, gistFile, gistLine, gistHighlightLine,
      gistHideLineNumbers, gistHideFooter, gistShowLoading, gistShowSpinner) {
    var $code;

    if (gistId) {
      $code = $('<code>').addClass('gist')
                        .attr('data-gist-id', gistId)
                        .attr('data-gist-file', gistFile)
                        .attr('data-gist-line', gistLine)
                        .attr('data-gist-highlight-line', gistHighlightLine)
                        .attr('data-gist-hide-line-numbers', gistHideLineNumbers)
                        .attr('data-gist-hide-footer', gistHideFooter)
                        .attr('data-gist-show-loading', gistShowLoading)
                        .attr('data-gist-show-spinner', gistShowSpinner);

      return $code[0];
    } else {
      return null;
    }
  };

  /**
   * @member plugin.gistembed
   * @private
   * @param {jQuery} $editable
   * @return {String}
   */
  var getTextOnRange = function ($editable) {
    $editable.focus();

    var rng = range.create();

    // if range on anchor, expand range with anchor
    if (rng.isOnAnchor()) {
      var anchor = dom.ancestor(rng.sc, dom.isAnchor);
      rng = range.createFromNode(anchor);
    }

    return rng.toString();
  };

  /**
   * Show Gist embed dialog and set event handlers on dialog controls.
   *
   * @member plugin.gistembed
   * @private
   * @param {jQuery} $dialog
   * @param {jQuery} $dialog
   * @param {Object} text
   * @return {Promise}
   */
  var showGistDialog = function ($editable, $dialog, text) {
    return $.Deferred(function (deferred) {
      var $gistDialog = $dialog.find('.note-gistembed-dialog');

      var $gistBtn = $gistDialog.find('.note-gistembed-btn'),
          $gistId = $gistDialog.find('#gist-id'),
          $gistFile = $gistDialog.find('#gist-file'),
          $gistLine = $gistDialog.find('#gist-line'),
          $gistHighlightLine = $gistDialog.find('#gist-highlight-line'),
          $gistHideLineNumbers = $gistDialog.find('#gist-hide-line-numbers'),
          $gistHideFooter = $gistDialog.find('#gist-hide-footer'),
          $gistShowLoading = $gistDialog.find('#gist-show-loading'),
          $gistShowSpinner = $gistDialog.find('#gist-show-spinner');

      $gistId.val('');
      $gistFile.val('');
      $gistLine.val('');
      $gistHighlightLine.val('');
      $gistHideLineNumbers.prop('checked', false);
      $gistHideFooter.prop('checked', false);
      $gistShowLoading.prop('checked', true);
      $gistShowSpinner.prop('checked', false);

      $gistDialog.one('shown.bs.modal', function () {
        $gistBtn.click(function (event) {
          event.preventDefault();

          deferred.resolve($gistId.val(), $gistFile.val(), $gistLine.val(), $gistHighlightLine.val(),
            $gistHideLineNumbers.prop('checked'), $gistHideFooter.prop('checked'),
            $gistShowLoading.prop('checked'), $gistShowSpinner.prop('checked'));

          $gistDialog.modal('hide');
        });
      }).one('hidden.bs.modal', function () {
        $gistBtn.off('click');

        if (deferred.state() === 'pending') {
          deferred.reject();
        }
      }).modal('show');
    });
  };

  $.summernote.addPlugin({
    /** @property {String} name name of plugin */
    name: 'gistembed',
    /**
     * @property {Object} buttons
     * @property {function(object): string} buttons.gistembed
     */
    buttons: {
      gistembed: function (lang, options) {
        return tmpl.iconButton(options.iconPrefix + 'github', {
          event: 'showGistDialog',
          title: lang.gist.gist,
          hide: true
        });
      }
    },

    /**
     * @property {Object} dialogs
     * @property {function(object, object): string} dialogs.gistembed
    */
    dialogs: {
      gistembed: function (lang) {
        var body = $('<form>' +
                        '<div class="form-group">' +
                          '<label for="gist-id">Gist ID</label>' +
                          '<input type="text" class="form-control" id="gist-id">' +
                        '</div>' +
                        '<div class="form-group">' +
                          '<label for="gist-file">Load a single file from a Gist</label>' +
                            '<input type="text" class="form-control" id="gist-file">' +
                          '<p class="help-block">(ex. "MyClass.java", "test.json").</p>' +
                        '</div>' +
                        '<div class="form-group">' +
                          '<label for="gist-line">Load single line</label>' +
                            '<input type="text" class="form-control" id="gist-line">' +
                          '<p class="help-block">Single line to load (ex. "2", "5").</p>' +
                        '</div>' +
                        '<div class="form-group">' +
                          '<label for="gist-highlight-line">Highlight lines</label>' +
                            '<input type="text" class="form-control" id="gist-highlight-line">' +
                          '<p class="help-block">Lines to highlight (ex. "2", "4-5", "2,4,6-9").</p>' +
                        '</div>' +
                        '<div class="checkbox">' +
                          '<label>' +
                            '<input type="checkbox" id="gist-hide-line-numbers"> Hide line numbers' +
                          '</label>' +
                        '</div>' +
                        '<div class="checkbox">' +
                          '<label>' +
                            '<input type="checkbox" id="gist-hide-footer"> Hide footer' +
                          '</label>' +
                        '</div>' +
                        '<div class="checkbox">' +
                          '<label>' +
                            '<input type="checkbox" id="gist-show-loading"> Show loading text' +
                          '</label>' +
                        '</div>' +
                        '<div class="checkbox">' +
                          '<label>' +
                            '<input type="checkbox" id="gist-show-spinner"> Show spinner' +
                          '</label>' +
                        '</div>' +
                      '</form>').html();

        var footer = '<button href="#" class="btn btn-primary note-gistembed-btn">' + lang.gist.gist + '</button>';
        return tmpl.dialog('note-gistembed-dialog', lang.gist.gist, body, footer);
      }
    },

    /**
     * @property {Object} events
     * @property {Function} events.showGistDialog
     */
    events: {
      showGistDialog: function (event, editor, layoutInfo) {
        var $dialog = layoutInfo.dialog(),
            $editable = layoutInfo.editable(),
            text = getTextOnRange($editable);

        // save current range
        editor.saveRange($editable);

        showGistDialog($editable, $dialog, text).then(function (gistId, gistFile, gistLine, gistHighlightLine,
            gistHideLineNumbers, gistHideFooter, gistShowLoading, gistShowSpinner) {
          // when ok button clicked

          // restore range
          editor.restoreRange($editable);

          // build node
          var $node = createCodeNode(gistId, gistFile, gistLine, gistHighlightLine, gistHideLineNumbers,
              gistHideFooter, gistShowLoading, gistShowSpinner);

          if ($node) {
            // insert the generated HTML
            editor.insertNode($editable, $node);
          }
        }).fail(function () {
          // when cancel button clicked
          editor.restoreRange($editable);
        });
      }
    },

    // define language
    langs: {
      'en-US': {
        gist: {
          gist: 'Gist',
          insert: 'Insert Gist'
        }
      }
    }
  });
}));
/**
 * PrismJS embed extension to the Summernote HTML5 WYSIWYG editor. Creates a
 * PRE & CODE tag with data-properties and CSS classes for the PrismJS script.
 */
(function (factory) {
  /* global define */
  if (typeof define === 'function' && define.amd) {
    // AMD. Register as an anonymous module.
    define(['jquery'], factory);
  } else {
    // Browser globals: jQuery
    factory(window.jQuery);
  }
}(function($) {
  // template
  var tmpl = $.summernote.renderer.getTemplate();

  // core functions: range, dom
  var range = $.summernote.core.range;
  var dom = $.summernote.core.dom;
  var first = true;

  /**
   * Create the pre/code HTML element
   *
   * @member plugin.prismembed
   * @private
   * @param {String} lang The language
   * @param {Boolean} lineNumbers Show line numbers
   * @param {String} highlight The line numbers to highlight
   * @param {String} content The code to syntax highlight
   * @return {Node}
   */
  var createCodeNode = function (lang, lineNumbers, highlight, content) {
    var $code,
        $pre;

    $pre = $('<pre></pre>');
    $code = $('<code></code>').addClass('prism language-' + lang)
                              .html(content);

    if (highlight) {
      $pre.attr('data-line', highlight);
    }

    if (lineNumbers) {
      $pre.addClass('line-numbers');
    }

    $pre.append($code);

    return $pre[0];
  };

  /**
   * @member plugin.prismembed
   * @private
   * @param {jQuery} $editable
   * @return {String}
   */
  var getTextOnRange = function ($editable) {
    $editable.focus();

    var rng = range.create();

    // if range on anchor, expand range with anchor
    if (rng.isOnAnchor()) {
      var anchor = dom.ancestor(rng.sc, dom.isAnchor);
      rng = range.createFromNode(anchor);
    }

    return rng.toString();
  };

  /**
   * Show Prism embed dialog and set event handlers on dialog controls.
   *
   * @member plugin.prismembed
   * @private
   * @param {jQuery} $dialog
   * @param {jQuery} $dialog
   * @param {Object} text
   * @return {Promise}
   */
  var showPrismDialog = function ($editable, $dialog, text) {
    return $.Deferred(function (deferred) {
      var $prismDialog = $dialog.find('.note-prismembed-dialog');

      var $prismBtn = $prismDialog.find('.note-prismembed-btn'),
          $lang = $prismDialog.find('#prism-lang'),
          $lineNumbers = $prismDialog.find('#prism-show-line-numbers'),
          $highlight = $prismDialog.find('#prism-highlight-line'),
          $content = $prismDialog.find('#prism-content');

      $content.val('');
      $highlight.val('');
      $lineNumbers.prop('checked', true);

      $prismDialog.one('shown.bs.modal', function () {
        $prismBtn.click(function (event) {
          event.preventDefault();

          deferred.resolve($lang.val(), $lineNumbers.prop('checked'), $highlight.val(), $content.val());

          $prismDialog.modal('hide');
        });
      }).one('hidden.bs.modal', function () {
        $prismBtn.off('click');

        if (deferred.state() === 'pending') {
          deferred.reject();
        }
      }).modal('show');
    });
  };

  $.summernote.addPlugin({
    /** @property {String} name name of plugin */
    name: 'prismembed',
    /**
     * @property {Object} buttons
     * @property {function(object): string} buttons.prismembed
     */
    buttons: {
      prismembed: function (lang, options) {
        return tmpl.iconButton(options.iconPrefix + 'code', {
          event: 'showPrismDialog',
          title: lang.prism.prism,
          hide: true
        });
      }
    },

    /**
     * @property {Object} dialogs
     * @property {function(object, object): string} dialogs.prismembed
    */
    dialogs: {
      prismembed: function (lang) {
        var body = $('<form>' +
                        '<div class="form-group">' +
                          '<label for="prism-lang">Language</label>' +
                          '<select class="form-control" id="prism-lang">' +
                            '<option value="markup">HTML</option>' +
                            '<option value="css">CSS</option>' +
                            '<option value="javascript">JavaScript</option>' +
                            '<option value="apacheconf">Apache Configuration</option>' +
                            '<option value="bash">Bash</option>' +
                            '<option value="git">Git</option>' +
                            '<option value="groovy">Groovy</option>' +
                            '<option value="handlebars">Handlebars</option>' +
                            '<option value="http">HTTP</option>' +
                            '<option value="java">Java</option>' +
                            '<option value="less">Less</option>' +
                            '<option value="markup">Markdown</option>' +
                            '<option value="sass">Sass</option>' +
                            '<option value="scss">Scss</option>' +
                            '<option value="scala">Scala</option>' +
                            '<option value="yaml">YAML</option>' +
                          '</select>' +
                        '</div>' +
                        '<div class="checkbox">' +
                          '<label>' +
                            '<input type="checkbox" id="prism-show-line-numbers"> Show line numbers' +
                          '</label>' +
                        '</div>' +
                        '<div class="form-group">' +
                          '<label for="prism-highlight-line">Highlight lines</label>' +
                            '<input type="text" class="form-control" id="prism-highlight-line">' +
                          '<p class="help-block">Lines to highlight (ex. "2", "4-5", "2,4,6-9").</p>' +
                        '</div>' +
                        '<div class="form-group">' +
                          '<label for="prism-content">Content</label>' +
                          '<textarea class="form-control" id="prism-content" rows="5"></textarea>' +
                        '</div>' +
                      '</form>').html();

        var footer = '<button href="#" class="btn btn-primary note-prismembed-btn">' + lang.prism.prism + '</button>';
        return tmpl.dialog('note-prismembed-dialog', lang.prism.prism, body, footer);
      }
    },

    /**
     * @property {Object} events
     * @property {Function} events.showPrismDialog
     */
    events: {
      showPrismDialog: function (event, editor, layoutInfo) {
        var $dialog = layoutInfo.dialog(),
            $editable = layoutInfo.editable(),
            text = getTextOnRange($editable);

        // save current range
        editor.saveRange($editable);

        showPrismDialog($editable, $dialog, text).then(function (lang, lineNumbers, highlight, content) {
          // when ok button clicked

          // restore range
          editor.restoreRange($editable);

          // build node
          var $node = createCodeNode(lang, lineNumbers, highlight, content);

          if ($node) {
            // insert the generated HTML
            editor.insertNode($editable, $node);
          }
        }).fail(function () {
          // when cancel button clicked
          editor.restoreRange($editable);
        });
      }
    },

    // define language
    langs: {
      'en-US': {
        prism: {
          prism: 'Prism',
          insert: 'Insert Prism Code Highlight'
        }
      }
    }
  });
}));
/**
 * Logout of the system
 */
$(function(){
  $('.logout').click(function(e){
    e.preventDefault();
    $.post('j_security_check', {
      j_username : '-',
      j_password : '-',
      j_validate : true
    }).always(function(data){
      if (data.status === 403) {
        window.location = '/admin/login.html'
      }
    });
  });
});
/**
 * Initialize the Summernote WYSIWYG HTML5/Bootstrap editor. This depends on the
 * custom slingasset summernote plugin.
 */
$(function(){
  $('.blog-edit-content').summernote({
    height: 300,
    toolbar : [
      ['group', ['undo', 'redo']],
      ['style', ['bold', 'italic', 'underline', 'clear']],
      ['font', ['strikethrough', 'superscript', 'subscript']],
      ['fontsize', ['fontsize']],
      ['color', ['color']],
      ['para', ['ul', 'ol', 'paragraph']],
      ['insert', ['slingasset', 'gistembed', 'prismembed', 'link', 'table', 'hr']],
      ['misc', ['fullscreen', 'codeview']],
      ['group', ['help']]
    ]
  });
});
/**
 * The Angular module for the rpgm administrative functions.
 */
var app = angular.module('rpgm', ['ngFileUpload', 'ui.bootstrap','xeditable']);
/**
 * Angular controller for adding and removing keywords/tags while editing blog
 * posts. Works with /libs/rpgm/components/admin/blogEdit component.
 */
app.controller('KeywordsController', function($scope){
  $scope.addKeyword = function(event) {
    event.preventDefault();
    $scope.keywords.push(null);
  };

  $scope.removeKeyword = function(event, index) {
    event.preventDefault();
    $scope.keywords.splice(index,1);
  }
});
/**
 * Angular controller to display comments for the admin panel as well as edit,
 * delete, mark as spam, and mark as ham (valid).
 */
app.controller('CommentController', function($scope, $modal, CommentService) {

  function openModal(action, index, callback) {
    var modalInstance = $modal.open({
      templateUrl: 'comment.html',
      controller: 'CommentModalController',
      resolve: {
        action: function() {
          return action;
        },
        comment: function() {
          return $scope.comments[index];
        }
      }
    });

    modalInstance.result.then(function(data){
      callback(data);
    });
  }

  $scope.comments = [];

  $scope.edit = function(index) {
    openModal('edit', index, function(data) {
      if (data.success) {
        $scope.comments[index] = data.comment;
      }
    });
  };

  $scope.akismet = function(index) {
    openModal('akismet', index, function(data) {
      if (data.success) {
        if (data.comment.spam) {
          $scope.comments.splice($scope.comments.indexOf(data.comment), 1);
        } else {
          $scope.comments[index] = data.comment;
        }
      }
    });
  };

  $scope.delete = function(index) {
    openModal('delete', index, function(data) {
      if (data.success) {
        $scope.comments.splice($scope.comments.indexOf(data.comment), 1);
      }
    });
  };

  /* Get all comments on load */
  CommentService.getComments().success(function(data){
    $scope.comments = data;
  });
});
/**
 * Angular controller to navigate and upload images and assets. The controller
 * works with the /libs/rpgm/components/assetList component.
 */
app.controller('AssetController', function($scope, $http, Upload) {

    $scope.breadcrumbs = ['assets'];
    $scope.currentPath = '/content/assets';
    $scope.folders = [];
    $scope.assets = [];

    $scope.getImagePath = function(image) {
      return $scope.currentPath + '/' + image;
    }

    $scope.navigate = function(folder, isRelative) {
      $scope.selectedAsset = null;

      if (isRelative) {
        if (folder === -1) {
          $scope.breadcrumbs.pop();
        } else {
          for (var x = folder - $scope.breadcrumbs.length + 1; x < 0; x++) {
            $scope.breadcrumbs.pop();
          }
        }
      } else {
        $scope.breadcrumbs.push(folder);
      }

      $scope.currentPath = '/content/' + $scope.breadcrumbs.join('/');
      update($scope.currentPath);
    };

    $scope.selectAsset = function(event) {
      $scope.selectedAsset = $(event.currentTarget).find('img').attr('src');
    };

    $scope.$watch('files', function() {
      $scope.upload($scope.files);
    });

    $scope.upload = function (files) {
      if (files && files.length) {
        for (var i = 0; i < files.length; i++) {
          var file = files[i];
          Upload.upload({
            url: '/bin/admin/uploadfile',
            file: file,
            fields: {'path' : $scope.currentPath},
            sendFieldsAs: 'form'
          }).progress(function (evt) {
            var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
            console.log('progress: ' + progressPercentage + '% ' + evt.config.file.name);
          }).success(function (data, status, headers, config) {
            console.log('file ' + config.file.name + 'uploaded. Response: ' + data);
            $scope.assets.push(file.name);
          });
        }
      }
    };

    function update(path) {
      $http.get(path + '.1.json')
        .success(function(data, status, headers, config) { 
          $scope.folders = [];
          $scope.assets = [];

          angular.forEach(data, function(value, key){
            if (!(new RegExp(/^(sling|jcr|rep):/).test(key))) {
              if (value['jcr:primaryType'] === 'nt:file') {
                $scope.assets.push(key);
              } else {
                $scope.folders.push(key);
              }
            }
          });
        })
        .error(function(data, status, headers, config) {
            // log error
        });
    }

    update('/content/assets');
});
/**
 * Angular controller to save settings through AJAX posts and display the proper
 * success or failure message. Works with all settings components including
 * system settings, email config, and reCAPTcha config.
 */
app.controller('SettingsController', function($scope, $attrs, SettingsService) {
  var ALERT_ERROR_CLASS = 'alert-danger',
      ALERT_SUCCESS_CLASS = 'alert-success';

  $scope.type = $attrs.settingsType;

  $scope.status = {
    show: false,
    type: null,
    header: null,
    message: null
  };

  $scope.$watchCollection('model', function() {
    $scope.hideAlert();
  });

  $scope.save = function($event) {
    $event.preventDefault();
    $scope.hideAlert();

    function show(type, header, message) {
      $scope.status.show = true;
      $scope.status.type = type;
      $scope.status.header = header;
      $scope.status.message = message;
    }

    SettingsService.updateSettings($scope.type, $scope.model)
      .then(function(result) {
          show(ALERT_SUCCESS_CLASS, result.data.header, result.data.message);
        }, function(result) {
          var header = 'Error',
              message = 'An error occured.';

          if (typeof result !== 'undefined' && result.data) {
            header = result.data.header;
            message = result.data.message;
          }

          show(ALERT_ERROR_CLASS, header, message);
      });
  };

  $scope.hideAlert = function() {
    $scope.status.show = false;
    $scope.status.type = null;
    $scope.status.header = null;
    $scope.status.message = null;
  };

  $scope.clear = function($event) {
    if ($event.target.type === 'password') {
      // TODO: find the proper way to access the model bound to the element that gained focus.
      $scope.model[angular.element($event.target).data('ngModel').replace(/model./gi, '')] = '';
    }
  };
});
/**
 * Angular controller to populate Groups and Users on the
 * /libs/rpgm/components/admin/userList component. Depends on the UserService
 * to communicate with the Sling server.
 */
app.controller('UserController', function($scope, $http, $modal, UserService) {

  $scope.userList = {
    groups:[
      {displayName: "Admin/Default", name: null, canUpdate : false, users: []},
      {displayName: "Authors", name: 'authors', canUpdate : true, users: []},
      {displayName: "Testers", name: 'testers', canUpdate : true, users: []}
    ]
  };

  $scope.delete = function(groupIndex, userIndex) {
    //TODO: create a confirmation modal
    UserService.deleteUser($scope.userList.groups[groupIndex].users[userIndex].user)
      .success(function(data){
        $scope.userList.groups[groupIndex].users.splice(userIndex ,1);
      });
  };

  $scope.edit = function(action, groupIndex, userIndex) {

    var modalInstance = $modal.open({
      templateUrl: 'user.html',
      controller: 'UserModalController',
      resolve: {
        action: function() {
          return action;
        },
        user: function() {
          return $scope.userList.groups[groupIndex].users[userIndex];
        },
        group: function() {
          return $scope.userList.groups[groupIndex].name;
        }
      }
    });

    modalInstance.result.then(function(changes) {
      if (!changes.data) {
        alert('error');
      } else {
        if (changes.action === 'updateUser') {
          $scope.userList.groups[groupIndex].users[userIndex].displayName = changes.data;
        } else if (changes.action === 'updatePass') {
          alert('password changed');
        } else {
          $scope.userList.groups[groupIndex].users.push({user: changes.data.user, displayName: changes.data.displayName});
        }
      }
    });
  };

  UserService.getAllUsers().success(function(data){
    angular.forEach(data, function(value, key){
      if (typeof value['memberOf'] !== 'undefined') {
        if (value['memberOf'][0] === '/system/userManager/group/authors') {
          $scope.userList.groups[1].users.push({user: key, displayName: value['displayName']});
        } else if (value['memberOf'][0] === '/system/userManager/group/testers') {
          $scope.userList.groups[2].users.push({user: key, displayName: value['displayName']});
        } else {
          $scope.userList.groups[0].users.push({user: key, displayName: value['displayName']});
        }
      }
    });
  });

});
/**
 * Angular controller for the User Controller Modals. The modals contain forms
 * to create and update users and groups. Depends on the UserService to
 * communicate with the Sling server.
 */
app.controller('UserModalController', function ($scope, $modalInstance, UserService, group, user, action) {

  $scope.group = group;
  $scope.isNew = action === 'add';
  $scope.isPass = action === 'pass';

  if (typeof user !== 'undefined') {
    $scope.user = user.user;
    $scope.displayName = user.displayName;
  }

  $scope.ok = function () {
    if ($scope.isNew) {
      UserService.createUser($scope.user, $scope.displayName, $scope.password, $scope.passwordConfirm).success(function(data){
        UserService.updateGroup($scope.group, $scope.user).success(function(data){
          $modalInstance.close({action: 'createUser', data: {user: $scope.user, displayName: $scope.displayName}});
        }).error(function(data){
          $modalInstance.close({action: 'createUser', data: false});
        });
      });
    } else if ($scope.isPass) {
      UserService.changePassword($scope.user, $scope.oldPassword, $scope.password, $scope.passwordConfirm).success(function(data){
        $modalInstance.close({action: 'updatePass', data: true});
      }).error(function(data){
        $modalInstance.close({action: 'updatePass', data: false});
      });
    } else {
      UserService.updateUser($scope.user, $scope.displayName).success(function(data){
        $modalInstance.close({action: 'updateUser', data: $scope.displayName});
      }).error(function(data){
        $modalInstance.close({action: 'updateUser', data: false});
      });
    }
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
/**
 * Required for Multipart/form-data file upload with AngularJS.
 */
app.factory('formDataObject', function() {
  return function(data, headersGetter) {
    var formData = new FormData();

    angular.forEach(data, function (value, key) {
      formData.append(key, value);
    });

    var headers = headersGetter();
    delete headers['Content-Type'];

    return formData;
  };
});
/**
 * Angular service to communicate with the Sling User Manager Post Servlet. This
 * service will save, update, and delete Jackrabbit users and groups.
 */
app.factory('UserService', function($http, formDataObject) {
  var userFactory          = {},
      PLACEHOLDER          = '{}',
      PATH_BASE            = '/system/userManager',
      PATH_USER_HOME       = PATH_BASE + '/user/{}',
      PATH_GET_ALL_USERS   = PATH_BASE + '/user.tidy.1.json',
      PATH_GET_USER        = PATH_BASE + '/user/{}.tidy.1.json',
      PATH_CREATE_USER     = PATH_BASE + '/user.create.json',
      PATH_UPDATE_USER     = PATH_BASE + '/user/{}.update.json',
      PATH_CHANGE_PASSWORD = PATH_BASE + '/user/{}.changePassword.json',
      PATH_DELETE_USER     = PATH_BASE + '/user/{}.delete.json',
      PATH_GET_ALL_GROUPS  = PATH_BASE + '/group.tidy.1.json',
      PATH_GET_GROUP       = PATH_BASE + '/group/{}.tidy.1.json',
      PATH_CREATE_GROUP    = PATH_BASE + '/group.create.json',
      PATH_UPDATE_GROUP    = PATH_BASE + '/group/{}.update.html', // Bug in Sling won't work with JSON
      PATH_DELETE_GROUP    = PATH_BASE + '/group/{}.delete.json';

  /**
   * @private
   */
  function post(path, data) {
    return $http({
      method: 'POST',
      url: path,
      data: data,
      transformRequest: formDataObject
    });
  }

  userFactory.getAllUsers = function() {
    return $http.get(PATH_GET_ALL_USERS);
  };

  userFactory.getUser = function(username) {
    return $http.get(PATH_GET_USER.replace(PLACEHOLDER, username));
  };

  userFactory.createUser = function(username, displayName, password, passwordConfirm) {
    return post(PATH_CREATE_USER, {
      ':name': username,
      pwd: password,
      pwdConfirm : passwordConfirm,
      displayName : displayName
    });
  };

  userFactory.updateUser = function(username, displayName) {
    return post(PATH_UPDATE_USER.replace(PLACEHOLDER, username), {
      displayName : displayName
    });
  };

  userFactory.changePassword = function(username, oldPassword, newPassword, newPasswordConfirm) {
    return post(PATH_CHANGE_PASSWORD.replace(PLACEHOLDER, username), {
      oldPwd : oldPassword,
      newPwd : newPassword,
      newPwdConfirm : newPasswordConfirm
    });
  };

  userFactory.deleteUser = function(username) {
    return post(PATH_DELETE_USER.replace(PLACEHOLDER, username), {
      go: 1
    });
  };

  userFactory.getAllGroups = function() {
    return $http.get(PATH_GET_ALL_GROUPS);
  };

  userFactory.getGroup = function(group) {
    return $http.get(PATH_GET_GROUP.replace(PLACEHOLDER, group));
  };

  userFactory.createGroup = function(group) {
    return post(PATH_CREATE_GROUP, {
      ':name': group
    });
  };

  userFactory.updateGroup = function(group, user) {
    return post(PATH_UPDATE_GROUP.replace(PLACEHOLDER, group), {
      ':member' : PATH_USER_HOME.replace(PLACEHOLDER, user)
    });
  };

  userFactory.deleteGroup = function(group) {
    return post(PATH_DELETE_GROUP.replace(PLACEHOLDER, group), {
      go: 1
    });
  };

  return userFactory;
});
/**
 * Angular service to communicate with the server-side for posting config
 * settings and returning success or failure messages.
 */
app.factory('SettingsService', function($http, formDataObject) {
  var settingsFactory = {},
      PATH_BASE = '/bin/admin',
      PATHS = {
        system    : PATH_BASE + '/systemconfig',
        email     : PATH_BASE + '/emailconfig',
        recaptcha : PATH_BASE + '/recaptchaconfig',
        akismet   : PATH_BASE + '/akismetconfig'
      };

  /**
   * @private
   */
  function post(path, data) {
    return $http({
      method: 'POST',
      url: path,
      data: data,
      transformRequest: formDataObject
    });
  }

  settingsFactory.updateSettings = function(type, model) {
    return post(PATHS[type], model);
  };

  return settingsFactory;
});
/**
 * Angular service to communicate with the Comment Admin Servlet. This service
 * will get all comments, delete comments, mark comments as spam, and mark
 * comments as ham (a valid comment).
 */
app.factory('CommentService', function($http, formDataObject) {
  var commentFactory = {},
      PATH = '/bin/admin/comment',
      DELETE_COMMENT = 'delete_comment',
      EDIT_COMMENT = 'edit_comment',
      MARK_SPAM = 'mark_spam',
      MARK_HAM = 'mark_ham'

  /**
   * @private
   */
  function post(data) {
    return $http({
      method: 'POST',
      url: PATH,
      data: data,
      transformRequest: formDataObject
    });
  }

  commentFactory.getComments = function() {
    return $http({
      method: 'GET',
      url: PATH
    });
  };

  commentFactory.deleteComment = function(comment) {
    return post({
      action: DELETE_COMMENT,
      id: comment.id
    });
  };

  commentFactory.editComment = function(comment) {
    return post({
      action: EDIT_COMMENT,
      id: comment.id,
      text: comment.comment
    });
  };

  commentFactory.submitSpam = function(comment) {
    return post({
      action: MARK_SPAM,
      id: comment.id
    });
  };

  commentFactory.submitHam = function(comment) {
    return post({
      action: MARK_HAM,
      id: comment.id
    });
  };

  return commentFactory;
});
/**
 * Angular controller for the Comment Controller Modals. The author can confirm
 * actions such as deletion, marking as spam, and marking as ham. The author can
 * also edit comments.
 */
app.controller('CommentModalController', function ($scope, $modalInstance, CommentService, action, comment) {

  $scope.comment = angular.copy(comment);
  $scope.editMode = action === 'edit';
  $scope.deleteMode = action === 'delete';
  $scope.spamMode = action === 'akismet' && !$scope.comment.spam;
  $scope.hamMode = action === 'akismet' && $scope.comment.spam;

  $scope.ok = function () {
    if ($scope.deleteMode) {
      CommentService.deleteComment($scope.comment).success(function(data){
        $modalInstance.close({success: true, comment: comment});
      });
    } else if ($scope.editMode) {
      CommentService.editComment($scope.comment).success(function(data){
        $scope.comment.edited = true;
        $modalInstance.close({success: true, comment: $scope.comment});
      });
    } else if ($scope.spamMode) {
      CommentService.submitSpam($scope.comment).success(function(data){
        $scope.comment.spam = true;
        $modalInstance.close({success: true, comment: $scope.comment});
      });
    } else if ($scope.hamMode) {
      CommentService.submitHam($scope.comment).success(function(data){
        $scope.comment.spam = false;
        $modalInstance.close({success: true, comment: $scope.comment});
      });
    }
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});
/**
 * Angular controller to upload, create, install, delete and list backup
 * packages.
 */
app.controller('BackupController', function($scope, $modal, BackupService) {

  $scope.packages = [];

  $scope.install = function(index) {
    openModal('install', index, function(data) {
      if (data.success) {
        // TODO: Display success/failure alerts
        alert('success');
      }
    });
  };

  $scope.delete = function(index) {
    openModal('delete', index, function(data) {
      if (data.success) {
        $scope.packages.splice(index, 1);
      }
    });
  };

  $scope.$watch('files', function() {
    if ($scope.files && $scope.files.length) {
      BackupService.uploadPackage($scope.files[0]).success(function(data, status, headers, config){
        if (data && data.data) {
          $scope.packages.unshift(JSON.parse(data.data));
        }
      });
    }
  });

  $scope.create = function() {
    openModal('create', null, function(data) {
      if (data.success) {
        $scope.packages.unshift(data.package);
      }
    });
  };

  function openModal(action, index, callback) {
    var modalInstance = $modal.open({
      templateUrl: 'package.html',
      controller: 'BackupModalController',
      resolve: {
        action: function() {
          return action;
        },
        package: function() {
          if (index != null) {
            return $scope.packages[index];
          } else {
            return null;
          }
        }
      }
    });

    modalInstance.result.then(function(data){
      callback(data);
    });
  }

  /* Get all packages on load */
  BackupService.getPackages().success(function(data){
    $scope.packages = data;
  });
});
/**
 * Angular service to communicate with the Backup Admin Servlet. This service
 * will get all packages, create packages, delete packages, install packages,
 * and upload packages.
 */
app.factory('BackupService', function($http, formDataObject, Upload) {
  var backupFactory = {},
      PATH = '/bin/admin/backup',
      ACTION_CREATE = 'create_package',
      ACTION_INSTALL = 'install_package',
      ACTION_UPLOAD = 'upload_package',
      ACTION_DELETE = 'delete_package';

  /**
   * @private
   */
  function post(data) {
    return $http({
      method: 'POST',
      url: PATH,
      data: data,
      transformRequest: formDataObject
    });
  }

  backupFactory.getPackages = function() {
    return $http({
      method: 'GET',
      url: PATH
    });
  };

  backupFactory.createBackup = function(name) {
    return post({
      action: ACTION_CREATE,
      name: name
    });
  };

  backupFactory.installBackup = function(name) {
    return post({
      action: ACTION_INSTALL,
      name: name
    });
  };

  backupFactory.deleteBackup = function(name) {
    return post({
      action: ACTION_DELETE,
      name: name
    });
  };

  backupFactory.uploadPackage = function(file) {
    return Upload.upload({
      url: PATH,
      file: file,
      fields: {action : ACTION_UPLOAD},
      sendFieldsAs: 'form'
    });
  };

  return backupFactory;
});
/**
 * Angular controller for the Backup Controller Modals. The author can confirm
 * actions such as deletion, installation, file upload, and package creation.
 */
app.controller('BackupModalController', function ($scope, $modalInstance, BackupService, action, package) {

  $scope.installMode = action == 'install';
  $scope.deleteMode = action == 'delete';
  $scope.createMode = action == 'create';
  $scope.package = package;

  $scope.ok = function () {
    if ($scope.createMode) {
      BackupService.createBackup($scope.name).success(function(data){
        $scope.package = JSON.parse(data.data);
        $modalInstance.close({success: true, package: $scope.package});
      });
    } else if ($scope.deleteMode) {
      BackupService.deleteBackup($scope.package.name).success(function(data){
        $modalInstance.close({success: true});
      });
    } else if ($scope.installMode) {
      BackupService.installBackup($scope.package.name).success(function(data){
        $modalInstance.close({success: true});
      });
    }
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
});