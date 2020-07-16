"use strict";
const express = require('express');
const router = express.Router();

let config = require('../config/config.js');
let mysql = require('mysql');
const connection = mysql.createConnection(config.database);

/* GET users listing. */
router.get('/:id/:category', function(req, res, next)
{
    let id = req.params.id;
    let category = req.params.category;
    if (!id || !category )
    {
        res.status(400).send("Bad request!");
        return;
    }

    const sql = 'SELECT e.`id` as `id`, fi.`text` as `text`, e.`owner_id` as `owner_id` FROM evaluations e '+
                'LEFT JOIN evaluationCompleded ec ON e.id = ec.`id` AND ec.`player_id` = ? '+
                'LEFT JOIN forms f ON f.`parent_id` = e.`owner_id` AND f.`category` = e.`category` '+
                'LEFT JOIN formItems fi ON fi.`parent_id` = f.`id` AND fi.`id` = e.`item_id` '+
                'WHERE e.owner_id != ? AND e.category = ? AND e.`totalVotes` < 20 AND ec.`player_id` IS NULL ORDER BY RAND()';
    connection.query(sql, [id, id, category], function (error, result) {
        if (error !== null)
        {
            res.status(500).send("Mysql error!");
            return;
        }

        let contain = [];
        let arrays = [];

        for (let i = 0; i < result.length; i++)
        {
            if (contain.includes(result[i].owner_id))
                continue;

            contain.push(result[i].owner_id);
            delete result[i].owner_id;
            arrays.push(result[i]);
        }

        if (arrays.length < 5)
        {
            res.status(204).send({});
            return;
        }

        res.send({rows: arrays});
    });

});

router.post('/:id/:category', function(req, res, next) {

    let id = req.params.id;
    let category = req.params.category;
    if (!id || !category )
    {
        res.status(400).send("Bad request!");
        return;
    }

    let obj = req.body;

    updateEvaluation(null,null, id, res, obj.list);
});

function updateEvaluation(error, result, player_id, res, list) {
    if (error !== null)
    {
        res.status(500).send("Mysql error!");
        return;
    }

    if (list.length === 0)
    {
        res.send({});
        return;
    }

    let sql = 'UPDATE evaluations SET yes = yes + ?, no = no + ?, totalVotes = totalVotes + 1, lastVoter = ? WHERE id = ?';
    let item = list.shift();
    query(sql,[item.res === 0 ? 1 : 0, item.res === 2 ? 1 : 0, player_id, item.id], updateEvaluation, player_id, res, list);
}

function query(sql, args, callback, player_id, res, array) {
    connection.query(sql, args, function (error, result) {
        callback(error,result, player_id, res, array);
    })
}

module.exports = router;
