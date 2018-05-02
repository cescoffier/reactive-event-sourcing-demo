class Patient {
    constructor(name, weight, height, age) {
        this.name = name;
        this.weight = weight;
        this.height = height;
        this.age = age;

        // Data
        this.diastolic = ["diastolic"];
        this.systolic = 120;
        this.temperature = ["temperature"];
        this.glucose = ["glucose"];

        console.log(`Creating patient: ${this.name}`);
        const self = this;
        this._insert(name, weight, height, age, function() {
            self.blood_pressure_sys_chart = self._createSystolicBloodPressureChart(`${name}-systolic-chart`);
            self.blood_pressure_dia_chart = self._createDiastolicBloodPressureChart(`${name}-diastolic-chart`);
            self.temperature_chart = self._createTemperatureDonut(`${name}-temperature-chart`);
            self.temperature_chart_line = self._createTemperatureLine(`${name}-temperature-line-chart`);
            self.glucose_chart = self._createGlucoseChart(`${name}-glucose-chart`);
        });
    }

    addMeasure(measure) {
        const dia = measure.diastolic;
        const sys = measure.systolic;
        const temp = measure.temperature;
        const gluc = measure.glucose;

        this.diastolic = Patient._add(this.diastolic, dia, 10);
        Patient._add(this.temperature, temp, 10);
        Patient._add(this.glucose, gluc, 5);
        this.systolic = sys;

        this._repaint();
    }

    _repaint() {
        const last_diastolic = this.diastolic.slice(-1).pop();
        const last_temperature = this.temperature.slice(-1).pop();
        
        this.blood_pressure_sys_chart.load( {
           columns: [
               ["Used", this.systolic],
               ["Available", 160 - this.systolic]
           ],
        });

        this.temperature_chart.load( {
            columns: [
                ["Used", last_temperature],
                ["Available", 45 - last_temperature]
            ],
        });

        this.glucose_chart.load( {
            columns: [
                this.glucose
            ],
        });

        // Update systolic value
        const title = d3.select(`#${this.name}-systolic-chart`).select('text.c3-chart-arcs-title');
        title.text("");
        title.insert('tspan').text(this.systolic.toFixed(1) +" | " + last_diastolic.toFixed(1))
                .classed('donut-title-small-pf', true).attr('dy', 0).attr('x', 0);

        // Update diastolic chart
        this.blood_pressure_dia_chart.load( {
           columns: [
              this.diastolic
           ]
        });

        // Update systolic value
        const temp_title = d3.select(`#${this.name}-temperature-chart`).select('text.c3-chart-arcs-title');
        temp_title.text("");
        temp_title.insert('tspan').text(last_temperature.toFixed(2) + " ºC" )
            .classed('donut-title-small-pf', true).attr('dy', 0).attr('x', 0);

        // Update diastolic chart
        this.blood_pressure_dia_chart.load( {
            columns: [
                this.diastolic
            ]
        });

        this.temperature_chart_line.load( {
            columns: [
                this.temperature
            ]
        });

    }

    _createSystolicBloodPressureChart(id) {
        console.log(id);
        const donutConfig = $().c3ChartDefaults().getDefaultDonutConfig("120/80");
        donutConfig.bindto = '#' + id;
        donutConfig.color = {
            pattern: ["#3f9c35", "#D1D1D1"]
        };
        donutConfig.data = {
            type: "donut",
            columns: [
                ["Used", 120],
                ["Available", 40]
            ],
            groups: [
                ["used", "available"]
            ],
            order: null
        };
        donutConfig.tooltip = {
            contents: function (d) {
                return '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                    Math.round(d[0].ratio * 100) + '%' + ' GB ' + d[0].name +
                    '</span>';
            }
        };

        const donutChartTitle = d3.select("#" + id).select('text.c3-chart-arcs-title');
        donutChartTitle.text("");
        donutChartTitle.insert('tspan').text("120/80").classed('donut-title-big-pf', true).attr('dy', 0).attr('x', 0);

        return c3.generate(donutConfig);
    }

    _createDiastolicBloodPressureChart(id) {
        const sparklineConfig = $().c3ChartDefaults().getDefaultSparklineConfig();
        sparklineConfig.bindto = '#' + id;
        sparklineConfig.data = {
            columns: [
                this.diastolic
            ],
            type: 'area'
        };
        sparklineConfig.axis = {
            y: {
                max: 100,
                min: 50,
                show: false
            },
            x: {
                show: false
            }
        };
        return c3.generate(sparklineConfig);
    }

    static _add(array, value, max) {
        if (array.length === max) {
            array.splice(1, 1);
        }
        array.push(value);
        return array;
    }

    _insert(name, weight, height, age, handler) {
        $.get('templates/patient-template.mst', function(template) {
            const rendered = Mustache.render(template, {
                name: name,
                height: height,
                weight: weight,
                age: age
            });
            $("#main").append($(rendered));
            handler();
        });
    }

   

    _createTemperatureDonut(id) {
        const config = $().c3ChartDefaults().getDefaultDonutConfig("37 ºC");
        config.bindto = '#' + id;
        config.color = {
            pattern: ["#EC7A08", "#D1D1D1"]
        };
        config.data = {
            type: "donut",
            columns: [
                ["Used", 37],
                ["Available", 10]
            ],
            groups: [
                ["used", "available"]
            ],
            order: null
        };
        config.tooltip = {
            contents: function (d) {
                return '<span class="donut-tooltip-pf" style="white-space: nowrap;">' +
                    Math.round(d[0].ratio * 100) + '%' + ' Gbps ' + d[0].name +
                    '</span>';
            }
        };

        const donutChartTitle = d3.select("#" + id).select('text.c3-chart-arcs-title');
        donutChartTitle.text("");
        donutChartTitle.insert('tspan').text("37 ºC").classed('donut-title-big-pf', true).attr('dy', 0).attr('x', 0);
        donutChartTitle.insert('tspan').text("Current").classed('donut-title-small-pf', true).attr('dy', 20).attr('x', 0);

        return c3.generate(config);
    }

    _createTemperatureLine(id) {
        const config = $().c3ChartDefaults().getDefaultSparklineConfig();
        config.bindto = '#' + id;
        config.data = {
            columns: [
                this.temperature,
            ],
            type: 'area'
        };
        config.axis = {
            y: {
                max: 43,
                min: 34,
                show: false
            },
            x: {
                show: false
            }
        };
        return c3.generate(config);
    }

    _createGlucoseChart(id) {
        const columnsData = [
            this.glucose
        ];
        const categories = [
            ['', '', '', '', '']
        ];
        const verticalBarChartConfig = $().c3ChartDefaults().getDefaultBarConfig(categories);
        verticalBarChartConfig.bindto = '#' + id;
        
        verticalBarChartConfig.data = {
            type: 'bar',
            columns: columnsData,
        };
        return c3.generate(verticalBarChartConfig);
    }
}
