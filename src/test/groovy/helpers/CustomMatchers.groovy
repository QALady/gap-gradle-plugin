package helpers

class CustomMatchers {
    public static GString sameString(Object value){
        return GString.EMPTY + value.toString()
    }

}