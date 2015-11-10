var express = require('express');
var crypto = require('crypto');
var app = express();

app.use(express.bodyParser());

function getHash(string) {
  return crypto
    .createHash('sha256')
    .update(string)
    .digest('hex')
    .substring(0, 8);
}

// get user data
app.get('/:id', function(req, res) {
  var o = {};
  var id = (req.params.id || '').toLowerCase();

  var key;

  if (id === 'meedamian' || id == 666) {
    key = '666';

    o.vanity = 'meeDamian';
    o.phone = '+886 909 377 026';
    o.location = {
      country: 'Taiwan',
      city: 'Taichung'
    };

  } else if (id === 'olek' || id == 420) {
    key = '420';

    o.vanity = 'Olek';
    o.location = {
      coutry: 'Poland',
      city: 'Olsztyn'
    };

  } else {
    key = id;

    o.phone = '+1 234 56789';
    o.location = {
      country: 'Neverland',
      city: 'Neversea'
    };
  }

  o.id = getHash(key);

  res.jsonp(o);
});

/** update user's vanity
 *
 * {
 *   key: ':key',
 *   vanity: ':vanity',
 *   id: getHash(':key'),
 *   phone: ':phone',
 *   location: ':location'
 * }
 */
app.post('/update', function(req, res) {
  res.json({
    all: req.body.message,
    vanity: null,
    phone: null,
    location: null
  });
});

app.listen();
