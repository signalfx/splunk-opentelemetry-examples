var express = require('express');
var router = express.Router();

/* GET users listing. */
router.get('/', function(req, res, next) {
  req.log.info('users.js endpoint invoked, sending response');
  res.send('respond with a resource');
});

module.exports = router;
