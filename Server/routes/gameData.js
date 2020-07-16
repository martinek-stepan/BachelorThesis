"use strict";
let express = require('express');
let router = express.Router();

if (!Array.from) Array.from = require('array-from');

let config = require('../config/config');
let mysql = require('mysql');
const connection = mysql.createConnection(config.database);


/* GET users listing. */
router.get('/changed/:accessCode', function(req, res, next) {
    let accessCode = req.params.accessCode;
    let obj = {};
    if (accessCode === '')
    {
        res.status(400).send('Username required!');
        return;
    }
    connection.query('SELECT accessCode FROM access_codes WHERE accessCode = ?', [accessCode], function (err, result) {
        if (err !== null)
        {
            handleError(res, err);
            return;
        }
        if (result.length === 0)
        {
            res.status(401).send('Unathorized access code');
            return;
        }

        connection.query('SELECT changedTimestamp FROM gameData WHERE accessCode = ?',[accessCode], function (error, results) {
            if (error !== null)
            {
                handleError(res, error);
                return;
            }

            if (results.length === 0)
            {
                res.status(418).send('Non existing user! Or maybe I am teapot');
                return;
            }

            results[0].changedTimestamp = parseInt(results[0].changedTimestamp);
            res.send(results[0]);
        });
    });
});

/* GET users listing. */
router.get('/get/:accessCode', function(req, res, next) {
    let accessCode = req.params.accessCode;
    if (accessCode === '')
    {
        res.status(400).send('Username required!');
        return;
    }
    connection.query('SELECT id, accessCode, playerName, totalMoney, changedTimestamp FROM gameData WHERE accessCode = ?',[accessCode], function (error, results) {
        if (error !== null)
        {
            handleError(res, error);
            return;
        }

        if (results.length === 0)
        {
            res.status(418).send('Non existing user! Or maybe I am teapot');
            return;
        }

        let obj = results[0];
        let sql1 = 'SELECT id, flowerType, name, waterLevel, mineralsLevel, fertilizerLevel, state, expirience, lastDecay FROM flowers WHERE parent_id = ? ORDER BY id ASC';
        connection.query(sql1,[obj.id],function (err1, res1) {
            if (err1 !== null)
            {
                handleError(res, err1);
                return;
            }
            obj.flowers = res1;

            let sql2 = 'SELECT id, category FROM forms WHERE parent_id = ?';
            connection.query(sql2,[obj.id], function (err2, res2) {
                if (err2 !== null)
                {
                    handleError(res, err2);
                    return;
                }
                res2.formItemList = [];
                obj.forms = res2;
                if (res2.length === 0)
                {
                    res.send(obj);
                    return;
                }
                let ids = '';
                for (let i=0; i < res2.length; i++)
                {
                    ids += res2[i].id;
                    if (i !== res2.length - 1)
                        ids += ',';
                }

                let sql3 = 'SELECT parent_id, `id`, `text`, `count` FROM formItems WHERE parent_id IN('+ids+') ORDER BY parent_id, id ASC';
                connection.query(sql3, [obj.id], function (err3, res3) {
                    if (err3 !== null)
                    {
                        handleError(res, err3);
                        return;
                    }
                    for (let i = 0; i < res3.length;i++)
                    {
                        let parent = res3[i].parent_id;
                        delete res3[i].parent_id;
                        for (let j = 0; j < obj.forms.length; j++)
                        {
                            if (parent === obj.forms[j].id)
                            {
                                if (obj.forms[j].formItemList === undefined)
                                    obj.forms[j].formItemList = [];
                                obj.forms[j].formItemList.push(res3[i]);
                            }
                        }
                    }

                    res.send(obj);
                });

            });
        });
    });
});

router.post('/', function(req, res, next) {

    let obj = req.body;
    obj.changedTimestamp = ''+Date.now();

    update(res, obj);
});

function processForms(error, results, res, obj, forms)
{
    if (error !== null || (results !== null && results.length === 0))
    {
        handleError(res, error);
        return;
    }

    if (forms.length === 0)
    {
        res.send(obj);
        return;
    }

    let form = forms.shift();
    let args = [(form.id === 0 ? null : form.id),obj.id,form.category];
    let items = Array.from(form.formItemList);
    queryItems('SELECT insertOrUpdateForm(?,?,?) as form_id', args, processItems, res, obj, items, form, forms);
}

function processItems(error, result, res, obj, items, form, forms) {

    if (error !== null || (result !== null && result.length === 0))
    {
        handleError(res, error);
        return;
    }

    if (items.length === 0)
    {
        processForms(null, null, res, obj, forms);
        return;
    }

    let item = items.shift();
    let args = [item.id, result[0].form_id, item.text, item.count];
    queryItems('SELECT insertOrUpdateFormItem(?,?,?,?) as form_id', args, processItems, res, obj, items, form, forms);
}

function processFlower(error, results, res, obj, flowers)
{
        if (error !== null)
    {
        handleError(res, error);
        return;
    }

    if (flowers.length === 0)
    {
        processForms(null, null, res, obj, Array.from(obj.forms));
        return;
    }

    let flower = flowers.shift();
    let args = [obj.id,flower.id, flower.flowerType, flower.name, flower.waterLevel, flower.mineralsLevel, flower.fertilizerLevel, flower.state, flower.expirience, flower.lastDecay];
    query('CALL insertOrUpdateFlower(?,?,?,?,?,?,?,?,?,?)', args, processFlower, res, obj, flowers);
}

function update(res, obj) {
    let args = [(obj.id === 0 ? null : obj.id), obj.accessCode, obj.playerName, obj.totalMoney, obj.changedTimestamp];
    connection.query('SELECT insertOrUpdateGameData(? ,?, ?, ?, ?) as id', args, function (error, result) {
        if (error !== null)
        {
            handleError(res, error);
            return;
        }

        obj.id = result[0].id;
        processFlower(null,null, res, obj, Array.from(obj.flowers));
    });
}

function query(sql, args, callback, res, obj, array) {
    connection.query(sql, args, function (error, result) {
        callback(error,result, res, obj, array);
    })
}

function queryItems(sql, args, callback, res, obj, array, form, forms) {
    connection.query(sql, args, function (error, result) {
        callback(error,result, res, obj, array, form, forms);
    })
}

function handleError(res, error) {
    console.trace(error);
    res.status(500).send('Mysql error!');
}

module.exports = router;
