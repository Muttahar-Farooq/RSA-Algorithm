package hu.unideb.ik;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    static BigInteger bigTwo = new BigInteger("2");
    static BigInteger bigOne = new BigInteger("1");
    static BigInteger bigZero = new BigInteger("0");

    static BigInteger[] extended_euclidean(BigInteger a, BigInteger b){

        BigInteger[] result = {a, bigOne, bigZero}; // contains values: gcd, x, y

        if (!b.equals(bigZero))
        {
            BigInteger[] tempResult = extended_euclidean(b, a.mod(b));
            result[0] = tempResult[0];  //a is replaced by b and b is replaced by mod of a/b
            result[1] = tempResult[2];
            result[2] = tempResult[1].subtract(tempResult[2].multiply(a.divide(b)));
        }

        return result;
    }

    static BigInteger fast_mod_pow(BigInteger a, BigInteger b, BigInteger m){
        BigInteger x = bigOne;
//        long y=a;
        while(b.compareTo(bigZero) > 0){
            if(b.mod(bigTwo).equals(bigOne)){
                x=x.multiply(a).mod(m);
            }
            a = a.pow(2).mod(m);
            b = b.shiftRight(1);
        }
        return x.mod(m);
    }

    /**
     * Returns true if the provided value is probably prime. Else returns false if the value is composite.
     * @param p prime number to check
     * @param a exponent
     * @return  true if the provided value is probably prime.
     */
    static boolean millerTest(BigInteger p, BigInteger a){

        BigInteger tempP = p.subtract(bigOne);
        BigInteger d;
        int s = 0;

        while(true){
            if(!tempP.testBit(0)){
                tempP = tempP.divide(bigTwo);
                s += 1;
            }
            else {
                d = tempP;
                break;
            }
        }
        if (fast_mod_pow(a,d,p).equals(bigOne))
            return true;
        else{
           for(int i=0;i<=s;i++)
               if(fast_mod_pow(a,new BigInteger("2").pow(i).multiply(d),p).equals(p.subtract(bigOne)))
                   return true;
        }
        return false;
    }

    static boolean multiMiilerTest(BigInteger p, int k,ArrayList<Integer> primes){
        BigInteger bigPrime;
        for (Integer prime : primes) {
            bigPrime = BigInteger.valueOf(prime);
            if (p.mod(bigPrime).equals(bigZero))
                return false;
            if (bigPrime.compareTo(p.sqrt()) > 0)
                break;
        }

        BigInteger a;
        while (k>0){
            k -= 1;
            a = randomBigInteger(p);
//            System.out.println(a);
            if(!millerTest(p,a))
                return false;
        }
        return true;
    }

    static BigInteger[] rsaKeysGenerator(int numbits, int iterations,ArrayList<Integer> primes){
        BigInteger p = bigPrimeGenerator(numbits,iterations,primes);
        BigInteger q = bigPrimeGenerator(numbits,iterations,primes);

        System.out.println("p="+p+"\nq="+q);

        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(bigOne).multiply(q.subtract(bigOne));
        BigInteger[] eea;
        BigInteger e;
        do {
            e = new BigInteger(phi.bitLength(), new Random());
            eea = extended_euclidean(e,phi);
        } while (e.compareTo(phi) >= 0 || e.compareTo(bigTwo) < 0 || !eea[0].equals(bigOne));

        BigInteger d = eea[1].mod(phi);

        return new BigInteger[]{n, e, d};
    }

    static String rsaEncrypt(String message, BigInteger n, BigInteger e){
        StringBuilder cipher = new StringBuilder();
        for (int i=0; i<message.length(); i++){
            int m = message.charAt(i);
            cipher.append(fast_mod_pow(BigInteger.valueOf(m), e, n)).append(" ");
        }
        return cipher.toString();
    }

    static String rsaDecrypt(String cipher, BigInteger n, BigInteger d){
        StringBuilder message = new StringBuilder();
        String[] cipherChars = cipher.split(" ");
        for(String c:cipherChars){
            message.append((char) fast_mod_pow(new BigInteger(c), d, n).intValue());
        }
        return message.toString();
    }

    static BigInteger randomBigInteger(BigInteger upperLimit){
        BigInteger randomNumber;
        BigInteger[] eea;
        do {
            randomNumber = new BigInteger(upperLimit.bitLength(), new Random());
            eea = extended_euclidean(randomNumber,upperLimit);
        } while (randomNumber.compareTo(upperLimit) >= 0 || randomNumber.compareTo(bigTwo) < 0 || !eea[0].equals(bigOne));
        return randomNumber;
    }

    static BigInteger bigPrimeGenerator(int numBits,int iterations,ArrayList<Integer> primes){
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(numBits, new Random());
        } while (randomNumber.compareTo(bigTwo) < 0 || !multiMiilerTest(randomNumber,iterations,primes));
        return randomNumber;
    }

    static ArrayList<Integer> sieveOfEratosthenes(int n){
        boolean[] prime = new boolean[n + 1];
        for (int i = 0; i <= n; i++)
            prime[i] = true;
        for (int p = 2; p * p <= n; p++) {
            if (prime[p]) {
                for (int i = p * p; i <= n; i += p)
                    prime[i] = false;
            }
        }
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 2; i <= n; i++) {
            if (prime[i])
                result.add(i);
        }
        return result;
    }

    public static void main(String[] args) {

        BigInteger a = new BigInteger("463");   //test variables
        BigInteger b = new BigInteger("547");
        BigInteger m = new BigInteger("47");
        ArrayList<Integer> primes = sieveOfEratosthenes((int)Math.pow(10,3));   //generated primes to sieve

//===============================RSA Algorithm===================================
        //====================Generating Keys===============
        BigInteger[] RSAkeys = rsaKeysGenerator(32,8,primes);  //generates 1024 bits long prime numbers
        System.out.println("private key =("+RSAkeys[2]+","+RSAkeys[0]+")");    //private key
        System.out.println("public key =("+RSAkeys[1]+","+RSAkeys[0]+")");    //public key

        //=====================Encryption=====================
        //Message to encrypt
        String message = """
        If it takes too long to generate primes, that’s probably, because you are using only the\040
        miller-rabin method to test if a number is prime. In practice it is not effective, because\040
        there are more composite numbers than prime numbers and with miller-rabin it’s super costly\040
        to tell about a composite number that it’s composite. One way is to do some tests before\040
        you would do miller-rabin. For example simply check with some small numbers if they divide\040
        your picked random number. In practice for example if you make a list of the first 100\040
        primes: 2, 3, 5, 7, ….. and you do a quick check (sieving) if any of these primes divide your\040
        picked random number, you can quickly tell about a lot of composite numbers that they are composite.\040
        If your picked number is not divided by any of those, it has a much better chance being a prime, so\040
        you can go on with the miller-rabin tests. (Typically for the sieving we are using all the primes\040
        under 10^6.
        """;
        String cipher = rsaEncrypt(message,RSAkeys[0],RSAkeys[1]);
        System.out.println("cipher=\n"+cipher);

        //=====================Decryption=====================
        String decryptedMessage = rsaDecrypt(cipher,RSAkeys[0],RSAkeys[2]);
        System.out.println("decrypted message=\n"+decryptedMessage);
//======================================================================================

    }
}
