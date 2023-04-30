import java.util.ArrayList;
import java.util.Arrays;
import java.util.SplittableRandom;
import java.lang.Math;

public class TSPUsingVBSS {
    private int[] tour;
    private ArrayList<Integer> heuristicValues;
    private double[] probabilities;
    private SplittableRandom sRand;
    private final double[] xValues;
    private final double[] yValues;
    private int B;

    public TSPUsingVBSS(int numOfCities, double[] xValues, double[]yValues, int confidenceInHeuristic){
        this.tour = new int[numOfCities];
        heuristicValues = new ArrayList<>();
        probabilities = new double[numOfCities];
        sRand = new SplittableRandom();
        this.xValues = xValues;
        this.yValues = yValues;
        B = confidenceInHeuristic;
    }

    /**
     * Generate a new random tour
     */
    public void generateNewTour(){
        int numberOfCities = tour.length;
        int[] newTour = new int[numberOfCities];

        for(int i = 0; i < numberOfCities; i++){
            newTour[i] = i;
        }
        for(int i = 0; i < numberOfCities   ; i++){
            int randomIndex1 = sRand.nextInt(numberOfCities);
            int randomIndex2 = sRand.nextInt(numberOfCities);
            swap(newTour, randomIndex1, randomIndex2);
        }
        this.tour = newTour;
    }

    /**
     * Helper method to create a random tour.
     * Simply swaps two cities in the tour
     * @param array - The array to manipulate
     * @param pos1 - The first city to swap
     * @param pos2 - The second city to swap
     */
    private void swap(int[] array, int pos1, int pos2){
        int temp = array[pos1];
        array[pos1] = array[pos2];
        array[pos2] = temp;
    }

    private void swap(double[] array, int pos1, int pos2){
        double temp = array[pos1];
        array[pos1] = array[pos2];
        array[pos2] = temp;
    }

    /**
     * Defined heuristic based on distance from start city.
     */
    public int[] costFromStartCity(int numOfCities){
        int costs[] = new int[numOfCities];
        int startingCity = tour.length - numOfCities;
        int cityCounter = startingCity;
        for(int i = 0; i < costs.length; i++){
            costs[i] = costBetweenTwoPoints(tour[startingCity], tour[cityCounter]);
            cityCounter++;
        }
        return costs;
    }

    public double inverseHeuristicSum(int[] costs) {
        int[] copyOfCosts = Arrays.copyOf(costs, costs.length);
        Arrays.sort(copyOfCosts);

        double[] heuristicArray = new double[costs.length];
        for (int i = 0; i < costs.length; i++) {
            int heuristicValue = Arrays.binarySearch(copyOfCosts, costs[i]);
            double temp = 1.0/(double)heuristicValue;
            heuristicArray[i] = temp;
            heuristicValues.add(heuristicValue);
        }
        double sum = Arrays.stream(heuristicArray).sum();
        return sum;
    }

    public int[] getHeuristic(int[] costs){
        int[] ranks = new int[costs.length];
        int[] copyOfCosts = Arrays.copyOf(costs, costs.length);
        quickSort(copyOfCosts, 0, costs.length-1);
        for(int i = 0; i < costs.length; i++){
            for(int j = 0; j < costs.length; j++){
                if(copyOfCosts[i] == costs[j]){
                    ranks[i] = j;
                }
            }
        }
        return ranks;

    }

    public void quickSort(int[] array, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(array, left, right);
            quickSort(array, left, pivotIndex - 1);
            quickSort(array, pivotIndex + 1, right);
        }
    }

    public int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left - 1;
        for (int j = left; j < right; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[right];
        arr[right] = temp;
        return i + 1;
    }


    /**
     * Selects new tour based on VBSS probability function
     */
    public void vbssSelection(){
        //Calculate denominator
        double denom = 0;
        for (int i = 1; i < heuristicValues.size(); i++){
            denom += 1.0 / Math.pow(heuristicValues.get(i), B);
        }

        //generate probabilities array
        for (int i = 0; i < heuristicValues.size(); i++){
            probabilities[i] = (1.0 / Math.pow(heuristicValues.get(i), B)) / denom;
        }

        //begin constructing a new tour
        int[] tempTour = new int[tour.length];
        int tempTourInc = 0;
        int[] visited = new int[tour.length];

        int visitedCitiesCounter = 0; //increment this each time a new city is found
        double rand;

        while(visitedCitiesCounter < tempTour.length){
            rand = sRand.nextDouble();

            //if the rand number is below the probability, then that city gets picked
            for (int i = 0; i < tempTour.length; i++){  //this doesn't work, you need to account for the fact that each probability has a range
                                                        //My best bet for a solution would be to sort the probabilities and keep the indexes in an array
                if (rand < probabilities[i]){
                    if (visited[i] > 0){    //if it's already in the loop, go back to generating another
                        break;
                    }

                    visitedCitiesCounter++; //incrementing counter
                    visited[i]++;
                    tempTour[tempTourInc++] = i;
                    break;
                }
            }
        }

    }

    /**
     * Generate the cost of the total tour
     * @param tour The current tour
     * @return The total cost of the tour
     */
    public int generateCost(int[] tour){
        int totalCost = 0;
        for (int j = 0; j < tour.length; j++) {
            int city1 = tour[j];
            int city2;
            if ((j + 1) < tour.length) {
                city2 = tour[j + 1];
            } else {
                city2 = tour[0];
            }
            totalCost += costBetweenTwoPoints(city1, city2);
        }
        return totalCost;
    }

    /**
     * Helper method for generateBestPopCost and generateCostOfIndividual
     * @param city1 - City one
     * @param city2 - City two
     * @return - The cost between the two cities
     */
    private int costBetweenTwoPoints(int city1, int city2){
        double x1 = xValues[city1];
        double x2 = xValues[city2];
        double y1 = yValues[city1];
        double y2 = yValues[city2];
        return (int)Math.round(Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
    }

    /**
     * Gives the current tour
     * @return The current tour
     */
    public int[] getTour(){
        return this.tour;
    }

    /**
     * Prints the current tour
     */
    public void printTour(){
        System.out.println(Arrays.toString(this.tour));
    }

    public void run(){

        generateNewTour();
        generateCost(tour);
    }

    public void run(int iterations){
        vbssSelection();
        System.out.println(generateCost(this.tour));
        for(int i = 0; i < iterations; i++){
            vbssSelection();
        }
        System.out.println(generateCost(this.tour));
    }

}