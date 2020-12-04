var baseURL = "https://glass-memento-289318.wl.r.appspot.com/companyHistoricalData/";

var ohlc = [],
        volume = [],
        // set the allowed units for data grouping
        groupingUnits = [[
            'week',                         // unit name
            [1]                             // allowed multiples
        ], [
            'month',
            [1, 2, 3, 4, 6]
        ]];
    
var chartOptions = {
    rangeSelector: {
        selected: 2
    },

   
    yAxis: [{
        startOnTick: false,
        endOnTick: false,
        labels: {
            align: 'right',
            x: -3
        },
        title: {
            text: 'OHLC'
        },
        height: '60%',
        lineWidth: 2,
        resize: {
            enabled: true
        }
    }, {
        labels: {
            align: 'right',
            x: -3
        },
        title: {
            text: 'Volume'
        },
        top: '65%',
        height: '35%',
        offset: 0,
        lineWidth: 2
    }],

    tooltip: {
        split: true
    },

    plotOptions: {
        series: {
            dataGrouping: {
                units: groupingUnits
            }
        }
    },

    series: [{
        type: 'candlestick',
        name: 'AAPL',
        id: 'aapl',
        zIndex: 2,
        data: ohlc
    }, {
        type: 'column',
        name: 'Volume',
        id: 'volume',
        data: volume,
        yAxis: 1
    }, {
        type: 'vbp',
        linkedTo: 'aapl',
        params: {
            volumeSeriesID: 'volume'
        },
        dataLabels: {
            enabled: false
        },
        zoneLines: {
            enabled: false
        }
    }, {
        type: 'sma',
        linkedTo: 'aapl',
        zIndex: 1,
        marker: {
            enabled: false
        }
    }]
}

const currentDate = new Date();
var startDate = "2018" + currentDate.toISOString().substring(4,10);
console.log(startDate);
var ticker = "goog";
xmlLoad();

function xmlLoad(){
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function(){
        if (this.readyState == 4 && this.status == 200){
            var xmlDoc = this.responseText;
            var obj = JSON.parse(xmlDoc);
            getCharts(obj)
        }
    };
    xhttp.open("GET",baseURL+ticker+"/"+startDate,true);
    xhttp.send();
}


function getCharts(obj){
    console.log(obj)
    for(let i = 0;i < obj.length; i++) {
        var entry = obj[i];
        ohlc.push([
            Date.parse(entry['date']),
            entry['open'],
            entry['high'],
            entry['low'],
            entry['close']
        ]);
        volume.push([
            Date.parse(entry['date']),
            entry['volume']
        ]);
    };

    chartOptions.series[0]['data'] = ohlc;
    chartOptions.series[1]['data'] = volume;
    Highcharts.stockChart('container', chartOptions);

}



    
        
        
