package utils;

public class PersistenceCalculator {
    public static int calculatePersistence(long number) {
        int persistence = 0;
        //itération tant que le nombre est supérieur ou égal à 10
        while (number >= 10) {
            long product = 1;
            //calcul du produit de tous les chiffres du nombre
            for (char digit : Long.toString(number).toCharArray()) {
                product *= Character.getNumericValue(digit);
            }
            number = product;
            persistence++;///Incrémentation de la persistanc
        }

        return persistence;
    }
}
