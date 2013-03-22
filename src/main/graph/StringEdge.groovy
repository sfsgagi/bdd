class StringEdge {

  static int counter = 1
  String text
  int id
  StringEdge(t) {
    text = t
    this.id = counter++
  }

  String toString(){
    return text
  }
}

