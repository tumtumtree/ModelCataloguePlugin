angular.module('mc.core.removableItemEnhancer', ['mc.util.rest', 'mc.util.enhance', 'mc.core.modelCatalogueApiRoot']).config ['enhanceProvider', (enhanceProvider)->
  condition = (item) -> item.hasOwnProperty('removeLink')
  factory   = ['modelCatalogueApiRoot', 'rest', '$rootScope', (modelCatalogueApiRoot, rest, $rootScope) ->
    (element, enhance = @enhance) ->
      link = "#{modelCatalogueApiRoot}#{element.removeLink}"
      element.remove = () ->
        enhance(rest(method: 'DELETE', url: link, data: element)).then (result)->
          $rootScope.$broadcast 'catalogueElementDeleted', element
          result

      element

  ]

  enhanceProvider.registerEnhancerFactory('removableItem', condition, factory)
]
