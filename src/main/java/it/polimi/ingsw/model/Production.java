package it.polimi.ingsw.model;

import it.polimi.ingsw.model.enums.ResourceType;

import java.util.HashMap;
import java.util.Map;

public class Production {
    private Map<ResourceType,Integer> input;
    private Map<ResourceType,Integer> output;
    private boolean selected;

    public Production(){
        input=new HashMap<>();
        output=new HashMap<>();
        selected=false;
    }

    /**
     * input get
     * @return the input of the production
     */
    public Map<ResourceType, Integer> getInput() {
        return input;
    }

    /**
     * output get
     * @return the output of the production
     */
    public Map<ResourceType, Integer> getOutput() {
        return output;
    }

    /**
     * selected get
     * @return true if the production is selected
     */
    public boolean checkSelected(){
        return selected;
    }

    /**
     * if production is selected, deselect it and vice versa
     */
    public void toggleSelected(){
        selected=!selected;
    }

    /**
     * adds to the input map a resource type and the corresponding quantity
     * @param resourceType type of input to be added
     * @param quantity quantity of the specified resource
     */
    public void addInput(ResourceType resourceType, Integer quantity) {
        if(input.containsKey(resourceType))
            input.replace(resourceType, input.get(resourceType) + quantity);
        else input.put(resourceType, quantity);
    }

    /**
     * adds to the output map a resource type and the corresponding quantity
     * @param resourceType type of output to be added
     * @param quantity quantity of the specified resource
     */
    public void addOutput(ResourceType resourceType, Integer quantity) {
        if(output.containsKey(resourceType))
            output.replace(resourceType, output.get(resourceType) + quantity);
        else output.put(resourceType, quantity);
    }
}
