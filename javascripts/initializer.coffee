# Sets up everything when the document loads.
$ ->
  view = new TestView()
  controller = new TestController view, 'ws://localhost:8081'

  # Debugging convenience.
  window.controller = controller
