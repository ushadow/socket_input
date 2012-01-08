class TestController
  constructor: (@view, @ws_uri)->
    view.onConnect = => @connect()
    view.onDisconnect = => @disconnect()
    
  connect: ->
    @view.showInfo 'Connecting'

    @ws = new WebSocket(@ws_uri) 
    @ws.onopen = => @onSocketOpen()
    @ws.onclose = => @onSocketClose()
    @ws.onerror = (errorMessage) => @onSocketError errorMessage
    @ws.onmessage = (event) => @onMessage event.data
  
  disconnect: ->
    @ws.close()
    
  onSocketOpen: ->
    @view.showInfo 'Connected'
    
  onSocketClose: ->
    @view.showInfo 'Disconnected'

  onSocketError: (errorMessage) ->
    @view.showInfo "WebSocket Error: #{errorMessage}"
    
  onMessage: (data) ->
    @view.showInfo "Server: #{data}"
