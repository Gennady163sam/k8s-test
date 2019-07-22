package com.analyzer.sysanalyzer.rest;

import com.analyzer.sysanalyzer.adapters.ClusterStatistics;
import com.analyzer.sysanalyzer.analyzers.Analyzer;
import com.analyzer.sysanalyzer.analyzers.ResourcesProperties;
import com.analyzer.sysanalyzer.states.CommandEnum;
import com.analyzer.sysanalyzer.states.StateMachine;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@Api("REST API for watching state cluster")
public class RestApiController {
    private StateMachine stateMachine;
    private ResourcesProperties resourcesProperties;
    private Analyzer analyzer;

    @Autowired
    public RestApiController(StateMachine stateMachine, ResourcesProperties resourcesProperties, Analyzer analyzer) {
        this.stateMachine = stateMachine;
        this.resourcesProperties = resourcesProperties;
        this.analyzer = analyzer;
    }

    @ApiOperation(value = "API for checking working app", response = String.class)
    @GetMapping("/ping")
    @ResponseBody
    public String hello() {
        return "pong";
    }

    @ApiOperation(value = "Changing state for state machine")
    @GetMapping("/changeState")
    public ResponseEntity changeState(@RequestParam @ApiParam(value="newState", example = "TURN_ON") String newState) {
        stateMachine.changeState(CommandEnum.valueOf(newState));
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/currentState")
    public ResponseEntity<String> getCurrentState() {
        return new ResponseEntity<>(stateMachine.getCurrentState(), HttpStatus.OK);
    }

    @GetMapping("/analyzing/enable")
    public ResponseEntity enableAnalyzing() {
        stateMachine.changeState(CommandEnum.TURN_ON);
        resourcesProperties.setAnalyzing(true);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/analyzing/disable")
    public ResponseEntity disableAnalyzing() {
        resourcesProperties.setAnalyzing(false);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/reconfigure")
    public ResponseEntity reconfigureSystem() {
        stateMachine.changeState(CommandEnum.RECONFIGURE);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/cluster")
    public ResponseEntity<ClusterStatistics> getClusterStatistics() {
        return new ResponseEntity<>(analyzer.getLastStatistics(), HttpStatus.OK);
    }

    @GetMapping("/cluster/averageCpu")
    public ResponseEntity getAverageCpu(@RequestParam String nodeName) {
        if (nodeName != null) {
            return new ResponseEntity<>(analyzer.getAverageCpuByNodeName(nodeName), HttpStatus.OK);
        }
        return new ResponseEntity<>(analyzer.getAllAverageCpu(), HttpStatus.OK);
    }

    @GetMapping("/cluster/averageMemory")
    public ResponseEntity getAverageMemory(@RequestParam String nodeName) {
        if (nodeName != null) {
            return new ResponseEntity<>(analyzer.getAverageMemoryByNodeName(nodeName), HttpStatus.OK);
        }
        return new ResponseEntity<>(analyzer.getAllAverageMemory(), HttpStatus.OK);
    }
}
