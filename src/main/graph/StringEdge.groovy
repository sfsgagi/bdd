class StringEdge {

  static int counter = 1
  String text
  String weight
  int id
  StringEdge(t) {
    text = t
    weight = t
    this.id = counter++
  }

  String toString(){
    return text
  }
}

