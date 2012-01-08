class TestView
  constructor: ->
    @$status = $('#status')
    
  showInfo: (message) ->
    @$status.text message