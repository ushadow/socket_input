# Sets up everything when the document loads.
$ ->
  view = new TestView()
  controller = new TestController view, 'ws://localhost'
  view.showInfo 'connecting'
    
      