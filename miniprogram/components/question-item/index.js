// components/question-item/index.js - 题目组件
// 同时支持选择题（1）与判断题（2）；答题后展示正误与解析
Component({
  properties: {
    // 题目对象：{ id, type, stem, options:[{key,content}], answer, analysis }
    question: {
      type: Object,
      value: null,
    },
    // 学生已选答案
    selectedAnswer: {
      type: String,
      value: '',
    },
    // 是否已作答（决定是否显示批改结果）
    answered: {
      type: Boolean,
      value: false,
    },
    // 正确答案（答题后由父级传入，用于高亮）
    correctAnswer: {
      type: String,
      value: '',
    },
  },

  methods: {
    /**
     * 点击选项：将选中 key 派发给父级处理
     */
    onOptionTap(e) {
      console.log(e,'2');
      this.triggerEvent('optiontap', { key: e.currentTarget.dataset.key });
    },
  },
});
