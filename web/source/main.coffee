express = require 'express'
crypto  = require 'crypto'
async   = require 'cloud/async.js'

app = express()

app.use express.badyParser()

getHash = (string) ->
  crypto.createHash 'sha256'
    .update '' + string
    .digest 'hex'
    .substring 0, 8

app.get '/:id', (req, res) ->
  o = {}
  id = (req.params.id or '').toLowerCase()

  key = ''

  if id in ['meedamian', 666]
    key = 666
    o =
      vanity: 'meeDamian'
      phone: '+886 909 377 026'
      location:
        country: 'Taiwan'
        city: 'Taichung'

  else if id in ['olek', 420]
    key = '420'

    o =
      vanity: 'Olek'
      location:
        coutry: 'Poland'
        city: 'Olsztyn'

  else
    key = id

    o =
      phone: '+1 234 56789'
      location:
        country: 'Neverland'
        city: 'Neversea'

  o.id = getHash key

  res.jsonp o


updaters =
  vanity: (key, newValue, cb) -> cb null, 'vanity'
  phone: (key, newValue, cb) -> cb null, 'phone'
  location: (key, newValue, cb) -> cb null, 'location'


app.post '/update', (req, res) ->
  key = req.body.key

  unless key
    res.json error: ['"key" missing']
    return

  fn = (name, key, newValue) -> (cb) ->
    updaters[name] key, newValue, cb

  queue = []
  for name of updaters
    val = req.body[name]
    if val
      queue.push fn name, key, val

  async.parallel queue, (err, results) ->
    res.json 200,
      id: getHash key
      updated: results

app.listen()
